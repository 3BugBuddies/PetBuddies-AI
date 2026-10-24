#!/usr/bin/env bash
set -euo pipefail

# Parâmetros vindos das variáveis da pipeline (LOCATION e NOME_WEBAPP)
location="${LOCATION:?LOCATION ausente}"
app="${NOME_WEBAPP:?NOME_WEBAPP ausente}"

# O banco é o Oracle da FIAP; as tabelas nascem pelas migrations do Flyway na subida do app

# ===================
# Atividades da Azure
# ===================
rg="rg-petbuddies"
plan="plan-petbuddies"
runtime="JAVA:21-java21"
sku="B1"

# Registrar o provedor utilizado
az provider register --namespace Microsoft.Web 1>/dev/null

# Criar os Recursos necessários em Nuvem (o plano é compartilhado com o Web App do .NET)
echo "Criando Grupo de Recursos..."
az group create --name "$rg" --location "$location" 1>/dev/null
echo "Criando Plano de Serviço..."
az appservice plan create --name "$plan" --resource-group "$rg" --location "$location" --sku "$sku" --is-linux 1>/dev/null
echo "Criando Serviço de Aplicativo..."
if ! az webapp show --resource-group "$rg" --name "$app" 1>/dev/null 2>&1; then
  az webapp create --resource-group "$rg" --plan "$plan" --runtime "$runtime" --name "$app" 1>/dev/null
fi
az webapp config set --resource-group "$rg" --name "$app" --always-on true 1>/dev/null

# Captura estado atual dos logs
app_logging="$(az webapp log show -g "$rg" -n "$app" --query 'applicationLogs.fileSystem.level' -o tsv 2>/dev/null || true)"
ws_logging="$(az webapp log show -g "$rg" -n "$app" --query 'httpLogs.fileSystem.enabled' -o tsv 2>/dev/null || true)"

# Condição: aplicar somente se algo não estiver habilitado como desejado
if [ "$app_logging" != "Information" ] || [ "$ws_logging" != "true" ]; then
  echo "Habilitando Logs do Serviço de Aplicativo..."
  az webapp log config \
    --resource-group "$rg" \
    --name "$app" \
    --application-logging filesystem \
    --web-server-logging filesystem \
    --level information 1>/dev/null
else
  echo "Logs já configurados"
fi

echo "[OK] Objetos da Azure prontos"
