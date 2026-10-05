# Copie este arquivo para scripts/config.local.sh e preencha os campos vazios.
# config.local.sh e ignorado pelo Git. Nunca coloque credenciais neste arquivo.
# Execute os scripts Bash no Azure Cloud Shell (Bash), como nas aulas.

RM="558771"
PROJECT_NAME="dimdim-atendimento"

# Preencha com a assinatura escolhida em az account list (aula 08).
SUBSCRIPTION_ID=""

# Escolha uma regiao permitida pelas politicas da SUA assinatura (aulas 08/15).
# Nao existe uma regiao presumidamente permitida para todas as contas.
LOCATION=""

RESOURCE_GROUP_NAME="rg-${PROJECT_NAME}-rm${RM}"
APP_SERVICE_PLAN="plan-${PROJECT_NAME}-rm${RM}"
WEBAPP_NAME="${PROJECT_NAME}-rm${RM}"
APP_INSIGHTS_NAME="insights-${PROJECT_NAME}-rm${RM}"
SQL_SERVER_NAME="sql-${PROJECT_NAME}-rm${RM}"
SQL_DATABASE_NAME="db-${PROJECT_NAME}"
RUNTIME="JAVA:17-java17"

# IP publico IPv4 do ambiente que executara o DDL via Invoke-Sqlcmd.
# Use somente um IP especifico. Se mudar de rede, atualize e repita o script 01.
SQL_CLIENT_IP=""

# Nomes de Web App e SQL Server sao globais no Azure. Em caso de conflito,
# acrescente um sufixo proprio aos dois nomes ANTES de criar os recursos.
# SQL_ADMIN_USER e SQL_ADMIN_PASSWORD serao pedidos sem eco nos scripts.
