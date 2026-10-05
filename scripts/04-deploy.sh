#!/usr/bin/env bash
# Aulas 13/14: mvn clean package e deploy automatizado com az webapp deploy --type jar.
# Sem argumentos: compila no ambiente atual. --jar CAMINHO: publica JAR ja compilado.
set +x
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/config.local.sh"
[[ -f "$CONFIG_FILE" ]] || { echo "Copie scripts/config.example.sh para scripts/config.local.sh e preencha-o." >&2; exit 1; }
# shellcheck source=config.example.sh
source "$CONFIG_FILE"
set +x
for variable in SUBSCRIPTION_ID RESOURCE_GROUP_NAME WEBAPP_NAME; do
  [[ -n "${!variable:-}" ]] || { echo "Preencha ${variable} em config.local.sh." >&2; exit 1; }
done
command -v az >/dev/null || { echo "Azure CLI ausente. Use o Azure Cloud Shell Bash." >&2; exit 1; }

if [[ "$#" == 0 ]]; then
  command -v mvn >/dev/null || { echo "Maven ausente. Use Maven/JDK 17 ou publique um artefato com --jar CAMINHO." >&2; exit 1; }
  command -v java >/dev/null || { echo "Java ausente. Use JDK 17 ou publique um artefato com --jar CAMINHO." >&2; exit 1; }
  cd -- "$PROJECT_DIR"
  echo "Compilando a aplicacao com Maven..."
  mvn clean package
  JAR_PATH="${PROJECT_DIR}/target/dimdim-atendimento-1.0.0.jar"
elif [[ "$#" == 2 && "$1" == "--jar" ]]; then
  # Caminhos relativos sao resolvidos a partir do diretorio em que voce executou.
  JAR_PATH="$2"
else
  echo "Uso: bash scripts/04-deploy.sh [--jar CAMINHO_DO_ARQUIVO.jar]" >&2
  exit 1
fi
[[ "$JAR_PATH" == *.jar && -f "$JAR_PATH" ]] || { echo "Informe um arquivo JAR local existente ou gere target/dimdim-atendimento-1.0.0.jar." >&2; exit 1; }
az account set --subscription "$SUBSCRIPTION_ID" --only-show-errors

# Politica SCM ensinada na aula 14; nenhuma credencial de publicacao e exportada.
az resource update --resource-group "$RESOURCE_GROUP_NAME" --namespace Microsoft.Web \
  --resource-type basicPublishingCredentialsPolicies --name scm --parent "sites/$WEBAPP_NAME" \
  --set properties.allow=true --output none --only-show-errors

echo "Publicando o JAR no Web App da Azure..."
az webapp deploy --resource-group "$RESOURCE_GROUP_NAME" --name "$WEBAPP_NAME" \
  --src-path "$JAR_PATH" --type jar --output none --only-show-errors
az webapp show --resource-group "$RESOURCE_GROUP_NAME" --name "$WEBAPP_NAME" \
  --query '{Aplicacao:name,Estado:state,Endereco:defaultHostName}' --output table --only-show-errors
echo "Deploy concluido. Abra o endereco HTTPS mostrado e execute os testes CRUD do README."
