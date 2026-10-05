#!/usr/bin/env bash
# Consultas de estado: nao liste appsettings, usuario SQL ou strings de conexao.
set +x
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/config.local.sh"
[[ -f "$CONFIG_FILE" ]] || { echo "Copie scripts/config.example.sh para scripts/config.local.sh e preencha-o." >&2; exit 1; }
# shellcheck source=config.example.sh
source "$CONFIG_FILE"
set +x
for variable in SUBSCRIPTION_ID RESOURCE_GROUP_NAME APP_SERVICE_PLAN WEBAPP_NAME APP_INSIGHTS_NAME SQL_SERVER_NAME SQL_DATABASE_NAME; do
  [[ -n "${!variable:-}" ]] || { echo "Preencha ${variable} em config.local.sh." >&2; exit 1; }
done
command -v az >/dev/null || { echo "Azure CLI ausente. Use o Azure Cloud Shell Bash." >&2; exit 1; }
az account set --subscription "$SUBSCRIPTION_ID" --only-show-errors

echo "Grupo de recursos:"
az group show --name "$RESOURCE_GROUP_NAME" \
  --query '{Grupo:name,Regiao:location,Estado:properties.provisioningState}' --output table --only-show-errors
echo "Plano de aplicativo:"
az appservice plan show --name "$APP_SERVICE_PLAN" --resource-group "$RESOURCE_GROUP_NAME" \
  --query '{Plano:name,Regiao:location,SKU:sku.name,Linux:reserved,Estado:status}' --output table --only-show-errors
echo "Web App:"
az webapp show --name "$WEBAPP_NAME" --resource-group "$RESOURCE_GROUP_NAME" \
  --query '{Aplicacao:name,Estado:state,Endereco:defaultHostName}' --output table --only-show-errors
echo "Azure SQL Server:"
az sql server show --name "$SQL_SERVER_NAME" --resource-group "$RESOURCE_GROUP_NAME" \
  --query '{Servidor:name,Regiao:location,Estado:state}' --output table --only-show-errors
echo "Azure SQL Database:"
az sql db show --name "$SQL_DATABASE_NAME" --server "$SQL_SERVER_NAME" --resource-group "$RESOURCE_GROUP_NAME" \
  --query '{Banco:name,Estado:status,Objetivo:currentServiceObjectiveName}' --output table --only-show-errors
echo "Application Insights:"
az monitor app-insights component show --app "$APP_INSIGHTS_NAME" --resource-group "$RESOURCE_GROUP_NAME" \
  --query '{Monitor:name,Regiao:location,Estado:provisioningState}' --output table --only-show-errors
echo "A verificacao de recursos nao substitui a prova do CRUD no banco e da telemetria no video."
