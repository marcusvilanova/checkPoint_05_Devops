#!/usr/bin/env bash
# Aula 14: configuracoes de ambiente, agente ~3 e conexao com Application Insights.
set +x
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/config.local.sh"
[[ -f "$CONFIG_FILE" ]] || { echo "Copie scripts/config.example.sh para scripts/config.local.sh e preencha-o." >&2; exit 1; }
# shellcheck source=config.example.sh
source "$CONFIG_FILE"
set +x
for variable in SUBSCRIPTION_ID RESOURCE_GROUP_NAME WEBAPP_NAME APP_INSIGHTS_NAME SQL_SERVER_NAME SQL_DATABASE_NAME; do
  [[ -n "${!variable:-}" ]] || { echo "Preencha ${variable} em config.local.sh." >&2; exit 1; }
done
command -v az >/dev/null || { echo "Azure CLI ausente. Use o Azure Cloud Shell Bash." >&2; exit 1; }
az account set --subscription "$SUBSCRIPTION_ID" --only-show-errors

trap 'unset SQL_ADMIN_USER SQL_ADMIN_PASSWORD CONNECTION_STRING' EXIT
read -r -s -p "Usuario administrador SQL (entrada oculta): " SQL_ADMIN_USER
printf '\n'
read -r -s -p "Senha administrador SQL (entrada oculta): " SQL_ADMIN_PASSWORD
printf '\n'
[[ -n "$SQL_ADMIN_USER" && -n "$SQL_ADMIN_PASSWORD" ]] || { echo "Usuario e senha nao podem estar vazios." >&2; exit 1; }

# A consulta fica em memoria; nao imprima a string nem grave um JSON de settings.
CONNECTION_STRING="$(az monitor app-insights component show --app "$APP_INSIGHTS_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" --query connectionString --output tsv --only-show-errors)"
[[ -n "$CONNECTION_STRING" && "$CONNECTION_STRING" != "null" && "$CONNECTION_STRING" != "None" ]] || { echo "Application Insights sem string de conexao. Revise o script 01." >&2; exit 1; }

echo "Aplicando configuracoes SQL e Application Insights sem exibir credenciais..."
if ! az webapp config appsettings set --name "$WEBAPP_NAME" --resource-group "$RESOURCE_GROUP_NAME" \
  --settings \
    "APPLICATIONINSIGHTS_CONNECTION_STRING=$CONNECTION_STRING" \
    'ApplicationInsightsAgent_EXTENSION_VERSION=~3' \
    'XDT_MicrosoftApplicationInsights_Mode=Recommended' \
    'XDT_MicrosoftApplicationInsights_PreemptSdk=1' \
    "SPRING_DATASOURCE_USERNAME=$SQL_ADMIN_USER" \
    "SPRING_DATASOURCE_PASSWORD=$SQL_ADMIN_PASSWORD" \
    "SPRING_DATASOURCE_URL=jdbc:sqlserver://${SQL_SERVER_NAME}.database.windows.net:1433;databaseName=${SQL_DATABASE_NAME};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;" \
  --output none --only-show-errors 2>/dev/null; then
  echo "Falha ao configurar o Web App. Verifique assinatura, permissoes e existencia dos recursos." >&2
  exit 1
fi
unset SQL_ADMIN_USER SQL_ADMIN_PASSWORD CONNECTION_STRING

# A connection string e o agente ~3 configurados acima integram o Web App ao Application Insights.
az webapp restart --name "$WEBAPP_NAME" --resource-group "$RESOURCE_GROUP_NAME" --output none --only-show-errors
echo "Configuracao concluida. Execute o script 04 para compilar e publicar o JAR."
