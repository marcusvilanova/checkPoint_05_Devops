#!/usr/bin/env bash
# Aulas 08, 14 e 15: Azure CLI, Web App, Azure SQL PaaS e Application Insights.
set +x
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/config.local.sh"
[[ -f "$CONFIG_FILE" ]] || { echo "Copie scripts/config.example.sh para scripts/config.local.sh e preencha-o." >&2; exit 1; }
# shellcheck source=config.example.sh
source "$CONFIG_FILE"
set +x

for variable in SUBSCRIPTION_ID LOCATION RESOURCE_GROUP_NAME APP_SERVICE_PLAN WEBAPP_NAME APP_INSIGHTS_NAME SQL_SERVER_NAME SQL_DATABASE_NAME RUNTIME SQL_CLIENT_IP; do
  [[ -n "${!variable:-}" ]] || { echo "Preencha ${variable} em config.local.sh." >&2; exit 1; }
done
[[ "$RUNTIME" == "JAVA:17-java17" ]] || { echo "Este projeto exige runtime Java 17." >&2; exit 1; }
[[ "$SQL_CLIENT_IP" =~ ^[0-9]{1,3}(\.[0-9]{1,3}){3}$ ]] || { echo "SQL_CLIENT_IP deve ser um unico IPv4 publico." >&2; exit 1; }
IFS='.' read -r -a ip_octets <<< "$SQL_CLIENT_IP"
for octet in "${ip_octets[@]}"; do
  ((10#$octet <= 255)) || { echo "SQL_CLIENT_IP invalido." >&2; exit 1; }
done
[[ "$SQL_CLIENT_IP" != "0.0.0.0" && "$SQL_CLIENT_IP" != "255.255.255.255" ]] || { echo "Informe o IP publico especifico do seu ambiente." >&2; exit 1; }
for resource_name in "$APP_SERVICE_PLAN" "$WEBAPP_NAME" "$APP_INSIGHTS_NAME" "$SQL_SERVER_NAME" "$SQL_DATABASE_NAME"; do
  [[ "$resource_name" =~ ^[a-z0-9][a-z0-9-]*[a-z0-9]$ ]] || { echo "Use nomes de recursos com letras minusculas, numeros e hifens." >&2; exit 1; }
done

command -v az >/dev/null || { echo "Azure CLI ausente. Use o Azure Cloud Shell Bash." >&2; exit 1; }
az account set --subscription "$SUBSCRIPTION_ID" --only-show-errors
az account show --query '{Assinatura:name,Estado:state}' --output table --only-show-errors

echo "Registrando os provedores apresentados em aula..."
az provider register --namespace Microsoft.Web --wait --output none --only-show-errors
az provider register --namespace Microsoft.Sql --wait --output none --only-show-errors
az provider register --namespace Microsoft.Insights --wait --output none --only-show-errors
az provider register --namespace Microsoft.OperationalInsights --wait --output none --only-show-errors
az extension add --name application-insights --output none --only-show-errors

echo "Criando ou reutilizando o grupo de recursos..."
az group create --name "$RESOURCE_GROUP_NAME" --location "$LOCATION" --output none --only-show-errors

existing_plan="$(az appservice plan list --resource-group "$RESOURCE_GROUP_NAME" --query "[?name=='${APP_SERVICE_PLAN}'].name | [0]" --output tsv --only-show-errors)"
if [[ -z "$existing_plan" ]]; then
  echo "Criando o plano Linux F1 (sem troca automatica para plano pago)..."
  az appservice plan create --name "$APP_SERVICE_PLAN" --resource-group "$RESOURCE_GROUP_NAME" \
    --location "$LOCATION" --sku F1 --is-linux --output none --only-show-errors
fi
plan_sku="$(az appservice plan show --name "$APP_SERVICE_PLAN" --resource-group "$RESOURCE_GROUP_NAME" --query sku.name --output tsv --only-show-errors)"
plan_kind="$(az appservice plan show --name "$APP_SERVICE_PLAN" --resource-group "$RESOURCE_GROUP_NAME" --query kind --output tsv --only-show-errors)"
[[ "$plan_sku" == "F1" && "$plan_kind" == *"linux"* ]] || { echo "O plano existente deve ser Linux F1. Nenhuma alteracao de plano foi feita." >&2; exit 1; }

existing_webapp="$(az webapp list --resource-group "$RESOURCE_GROUP_NAME" --query "[?name=='${WEBAPP_NAME}'].name | [0]" --output tsv --only-show-errors)"
if [[ -z "$existing_webapp" ]]; then
  echo "Criando o Web App Java 17..."
  az webapp create --name "$WEBAPP_NAME" --resource-group "$RESOURCE_GROUP_NAME" \
    --plan "$APP_SERVICE_PLAN" --runtime "$RUNTIME" --output none --only-show-errors
fi

# Nao repita a criacao do servidor existente: isso poderia redefinir credenciais.
existing_sql="$(az sql server list --resource-group "$RESOURCE_GROUP_NAME" --query "[?name=='${SQL_SERVER_NAME}'].name | [0]" --output tsv --only-show-errors)"
if [[ -z "$existing_sql" ]]; then
  trap 'unset SQL_ADMIN_USER SQL_ADMIN_PASSWORD' EXIT
  read -r -s -p "Usuario administrador SQL (entrada oculta): " SQL_ADMIN_USER
  printf '\n'
  read -r -s -p "Senha administrador SQL (entrada oculta): " SQL_ADMIN_PASSWORD
  printf '\n'
  [[ -n "$SQL_ADMIN_USER" && -n "$SQL_ADMIN_PASSWORD" ]] || { echo "Usuario e senha nao podem estar vazios." >&2; exit 1; }
  echo "Criando o servidor SQL PaaS..."
  # Nao exiba a resposta nem os erros deste comando, que podem conter o usuario.
  if ! az sql server create --name "$SQL_SERVER_NAME" --resource-group "$RESOURCE_GROUP_NAME" \
    --location "$LOCATION" --admin-user "$SQL_ADMIN_USER" --admin-password "$SQL_ADMIN_PASSWORD" \
    --enable-public-network true --output none --only-show-errors 2>/dev/null; then
    echo "Falha ao criar SQL. Verifique politicas/regiao, nome global e requisitos das credenciais no Portal Azure." >&2
    exit 1
  fi
  unset SQL_ADMIN_USER SQL_ADMIN_PASSWORD
else
  echo "Servidor SQL existente reutilizado; credenciais preservadas."
fi

existing_db="$(az sql db list --server "$SQL_SERVER_NAME" --resource-group "$RESOURCE_GROUP_NAME" --query "[?name=='${SQL_DATABASE_NAME}'].name | [0]" --output tsv --only-show-errors)"
if [[ -z "$existing_db" ]]; then
  echo "Criando o Azure SQL Database Basic (servico sujeito a cobranca)..."
  az sql db create --resource-group "$RESOURCE_GROUP_NAME" --server "$SQL_SERVER_NAME" \
    --name "$SQL_DATABASE_NAME" --service-objective Basic --backup-storage-redundancy Local \
    --zone-redundant false --output none --only-show-errors
fi
db_objective="$(az sql db show --name "$SQL_DATABASE_NAME" --server "$SQL_SERVER_NAME" --resource-group "$RESOURCE_GROUP_NAME" --query currentServiceObjectiveName --output tsv --only-show-errors)"
[[ "$db_objective" == "Basic" ]] || { echo "O banco existente deve usar objetivo Basic. Nenhuma alteracao de tamanho foi feita." >&2; exit 1; }

# Mesmo comando da aula 15, com regras delimitadas em vez de liberaGeral.
# 0.0.0.0/0.0.0.0 significa permitir servicos Azure, nao todos os IPs da Internet.
# Essa opcao abrange servicos de outras assinaturas Azure; autenticacao segue exigida.
echo "Configurando o firewall SQL para servicos Azure e o IP informado..."
az sql server firewall-rule create --resource-group "$RESOURCE_GROUP_NAME" --server "$SQL_SERVER_NAME" \
  --name permitir-servicos-azure --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0 \
  --output none --only-show-errors
az sql server firewall-rule create --resource-group "$RESOURCE_GROUP_NAME" --server "$SQL_SERVER_NAME" \
  --name cliente-ddl --start-ip-address "$SQL_CLIENT_IP" --end-ip-address "$SQL_CLIENT_IP" \
  --output none --only-show-errors

echo "Criando ou reutilizando o Application Insights..."
az monitor app-insights component create --app "$APP_INSIGHTS_NAME" --location "$LOCATION" \
  --resource-group "$RESOURCE_GROUP_NAME" --application-type web --output none --only-show-errors

echo "Recursos prontos. Proximo passo: executar scripts/02-aplicar-ddl.ps1 no PowerShell."
