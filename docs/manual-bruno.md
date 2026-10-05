# Manual de execução e gravação - Bruno Ferreira

Este manual é o roteiro operacional para executar o Check Point 5, validar a aplicação na Azure e gravar o vídeo. Siga a ordem indicada. O vídeo precisa mostrar a execução real na nuvem; uma tela de código, um teste em localhost ou um recurso criado anteriormente não substituem as evidências.

## Resultado que precisa ser entregue

Ao final, devem existir:

1. Uma aplicação Java 17 publicada no Azure App Service Linux.
2. Um Azure SQL Database Basic com as tabelas `dbo.clientes` e `dbo.atendimentos`.
3. Uma chave estrangeira de `atendimentos.cliente_id` para `clientes.id`.
4. CRUD completo das duas tabelas pelo frontend.
5. Uma gravação narrada, com pelo menos 720p, mostrando criação dos recursos, DDL, deploy, oito operações com consulta SQL depois de cada operação e Application Insights.
6. Um repositório GitHub acessível ao professor e um PDF no Teams contendo somente nome do grupo, nomes/RMs e os links.

O responsável pela execução e pela gravação é Bruno Ferreira. Os demais integrantes devem acompanhar a execução, conferir as telas e revisar os links antes do envio.

## Antes de abrir o terminal

Use dados fictícios no vídeo. Separe quatro janelas:

- navegador com o portal Azure e a aplicação publicada;
- Cloud Shell Bash para Azure CLI;
- Cloud Shell PowerShell para `Invoke-Sqlcmd`;
- cliente SQL ou o PowerShell com `Invoke-Sqlcmd` para consultar o banco.

Não deixe senha, token, string de conexão, e-mail de login ou valor de `SPRING_DATASOURCE_PASSWORD` visível na gravação. Ao pedir uma senha, pare a gravação ou cubra a área do terminal.

O vídeo deve ser gravado em resolução mínima 1280x720, com microfone funcionando. Antes da gravação definitiva, faça um ensaio até a aplicação abrir e até uma consulta SQL retornar dados.

## 1. Conferir a conta Azure e a assinatura

Abra o portal Azure com a conta da turma e abra o Cloud Shell em **Bash**. Rode:

```bash
az account list \
  --query "[].{Nome:name,Id:id,Estado:state}" \
  --output table
```

Escolha uma assinatura cujo estado seja `Enabled`. Se a assinatura aparecer como `Disabled`, não rode o script de criação: o Azure não permitirá criar ou publicar recursos. Reative a assinatura ou peça ao professor uma assinatura ativa.

Anote o ID da assinatura ativa. Ele será colocado em `SUBSCRIPTION_ID` no arquivo local de configuração.

Confira regiões disponíveis, se necessário:

```bash
az account list-locations \
  --query "[?metadata.regionType=='Physical'].{Nome:name,Display:displayName}" \
  --output table
```

Use uma região liberada pela política da assinatura. Não troque o plano F1 por um plano pago para contornar quota ou região.

## 2. Baixar o repositório e preparar a configuração

Substitua `URL_DO_REPOSITORIO` pelo endereço final do GitHub:

```bash
git clone URL_DO_REPOSITORIO
cd NOME_DA_PASTA
cp scripts/config.example.sh scripts/config.local.sh
```

Descubra o IP público da sessão que executará o DDL:

```bash
curl -4 ifconfig.me
```

Abra o arquivo:

```bash
nano scripts/config.local.sh
```

Preencha os campos abaixo. Os nomes precisam ser minúsculos, sem acentos e sem espaços:

```bash
RM="558771"
PROJECT_NAME="dimdim-atendimento"
SUBSCRIPTION_ID="ID_DA_ASSINATURA_ENABLED"
LOCATION="regiao-permitida"
SQL_CLIENT_IP="IP_PUBLICO_DO_CLOUD_SHELL_OU_PC"
```

Os nomes derivados serão:

| Recurso | Nome gerado |
| --- | --- |
| Grupo de recursos | `rg-dimdim-atendimento-rm558771` |
| Plano App Service | `plan-dimdim-atendimento-rm558771` |
| Web App | `dimdim-atendimento-rm558771` |
| Application Insights | `insights-dimdim-atendimento-rm558771` |
| SQL Server lógico | `sql-dimdim-atendimento-rm558771` |
| Banco | `db-dimdim-atendimento` |

Web App e SQL Server precisam de nomes globais. Se um nome já existir, altere `PROJECT_NAME` antes de criar qualquer recurso. Nunca salve usuário ou senha SQL em `config.local.sh`; os scripts solicitam esses valores sem eco.

Confirme a sintaxe antes de criar recursos:

```bash
for arquivo in scripts/*.sh; do bash -n "$arquivo" || exit 1; done
echo "Sintaxe Bash OK"
```

## 3. Criar os recursos Azure

Com a gravação iniciada, explique que o projeto usa App Service, Azure SQL PaaS e Application Insights. Depois execute:

```bash
bash scripts/01-criar-recursos.sh
```

Quando solicitado, informe o usuário administrador do SQL e uma senha que siga a política do Azure. A entrada fica oculta. O script cria ou valida:

- grupo de recursos;
- plano Linux F1;
- Web App com runtime `JAVA:17-java17`;
- SQL Server lógico;
- Azure SQL Database com objetivo `Basic`;
- regra para serviços Azure e regra para o IP público específico;
- componente Application Insights.

O script é repetível. Se o SQL Server já existir, ele não pede a senha novamente nem redefine credenciais. Se o plano existente não for Linux F1 ou o banco não for Basic, o script para sem alterar o SKU.

Depois mostre no portal os nomes, a região e o estado de provisionamento. Não mostre as configurações privadas do Web App.

## 4. Aplicar o DDL no Azure SQL

Abra uma sessão PowerShell no Cloud Shell. O arquivo de configuração Bash não precisa ser carregado no PowerShell. Execute o DDL informando os nomes reais:

```powershell
Get-Command Invoke-Sqlcmd
./scripts/02-aplicar-ddl.ps1 `
  -ServerName sql-dimdim-atendimento-rm558771 `
  -DatabaseName db-dimdim-atendimento
```

O script pede usuário e senha sem exibir os valores e executa `scripts/ddl.sql`. O DDL é idempotente: não apaga tabelas nem dados existentes. Ele cria:

- `dbo.clientes`: `id`, `nome`, `email`, `telefone`;
- `dbo.atendimentos`: `id`, `assunto`, `descricao`, `status`, `data_abertura`, `cliente_id`;
- `FK_atendimentos_clientes`, com `ON DELETE NO ACTION`;
- índice `IX_atendimentos_cliente_id`.

Confirme a estrutura:

```powershell
Invoke-Sqlcmd `
  -ServerInstance "sql-dimdim-atendimento-rm558771.database.windows.net" `
  -Database "db-dimdim-atendimento" `
  -Credential (Get-Credential) `
  -Query "SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='dbo';"
```

Não grave a senha na linha de comando. Se o IP mudou desde o script 01, atualize somente a regra desse IP no firewall e repita a consulta.

## 5. Configurar a aplicação e o monitoramento

Volte ao Cloud Shell Bash e execute:

```bash
bash scripts/03-configurar-aplicacao.sh
```

Informe a mesma credencial do SQL. O script configura a URL JDBC criptografada e as variáveis do Spring:

- `SPRING_DATASOURCE_URL`;
- `SPRING_DATASOURCE_USERNAME`;
- `SPRING_DATASOURCE_PASSWORD`;
- `APPLICATIONINSIGHTS_CONNECTION_STRING`;
- `ApplicationInsightsAgent_EXTENSION_VERSION=~3`;
- `XDT_MicrosoftApplicationInsights_Mode=Recommended`.

O script conecta o Application Insights ao Web App e reinicia o serviço. Aguarde o reinício terminar.

## 6. Compilar e publicar o JAR

### Opção A - compilar e publicar no mesmo ambiente

Se Maven e JDK 17 estiverem disponíveis na sessão:

```bash
mvn clean verify
bash scripts/04-deploy.sh
bash scripts/05-verificar-recursos.sh
```

O build precisa mostrar `BUILD SUCCESS` e gerar `target/dimdim-atendimento-1.0.0.jar`. A suíte local possui 28 testes.

### Opção B - compilar no PC e enviar somente o JAR

No PC com JDK 17 e Maven:

```powershell
mvn clean verify
```

No Cloud Shell, use o botão de upload para enviar o arquivo `target/dimdim-atendimento-1.0.0.jar` para a pasta `target` do clone. Depois execute:

```bash
bash scripts/04-deploy.sh --jar target/dimdim-atendimento-1.0.0.jar
bash scripts/05-verificar-recursos.sh
```

Nesse caminho o Cloud Shell precisa de Azure CLI, mas não precisa de Maven nem Java para o deploy.

O script 04 usa exatamente `az webapp deploy --type jar`. Copie o hostname exibido pelo script e abra:

```text
https://NOME_DO_WEBAPP.azurewebsites.net
```

Não prossiga para o vídeo se a página não carregar. O aplicativo usa `ddl-auto=validate`, portanto erro de tabela, coluna ou credencial precisa ser corrigido no SQL/configuração.

## 7. Rotas do aplicativo

Estas são as páginas que Bruno deve mostrar no navegador:

| Rota | Função |
| --- | --- |
| `/` | resumo da aplicação |
| `/clientes` | lista de clientes |
| `/clientes/novo` | formulário de cadastro |
| `/clientes/{id}` | detalhe do cliente e seus atendimentos |
| `/clientes/{id}/editar` | edição do cliente |
| `/atendimentos` | lista de atendimentos |
| `/atendimentos/novo` | formulário de cadastro |
| `/atendimentos/{id}` | detalhe do atendimento e cliente associado |
| `/atendimentos/{id}/editar` | edição do atendimento |

As exclusões são feitas pelos botões da tela usando POST. O sistema bloqueia a exclusão de cliente que ainda possui atendimento.

## 8. Sequência exata do teste CRUD

Use um cliente fictício identificável, por exemplo:

```text
Nome: Bruno Ferreira - CP5
E-mail: bruno.cp5.558771@example.com
Telefone: (11) 99999-5587
```

Use um atendimento fictício:

```text
Assunto: Validacao do atendimento CP5
Descricao: Registro criado durante a demonstracao do Check Point 5.
Status: Aberto
Data: data atual
```

Após cada ação no frontend, alterne imediatamente para o SQL e execute a consulta correspondente. Não deixe as oito consultas para o final.

### 8.1 CREATE cliente

No navegador, abra `/clientes/novo`, preencha os três campos e salve. Anote o `id` mostrado no detalhe. No SQL:

```sql
SELECT id, nome, email, telefone
FROM dbo.clientes
WHERE email = 'bruno.cp5.558771@example.com';
```

O resultado precisa conter uma linha com o cliente recém-criado.

### 8.2 READ cliente

Abra a listagem e o detalhe do cliente. No SQL, repita a consulta pelo e-mail e compare ID, nome, telefone e e-mail com a tela.

### 8.3 UPDATE cliente

Edite o telefone para `(11) 98888-5587` e salve. No SQL:

```sql
SELECT id, nome, email, telefone
FROM dbo.clientes
WHERE email = 'bruno.cp5.558771@example.com';
```

Mostre que o mesmo ID permanece e que o telefone mudou.

### 8.4 CREATE atendimento

Abra `/atendimentos/novo`, selecione o cliente, preencha o assunto e a descrição e salve. Anote o ID do atendimento. No SQL:

```sql
SELECT a.id, a.assunto, a.descricao, a.status,
       a.data_abertura, a.cliente_id, c.nome AS cliente
FROM dbo.atendimentos AS a
JOIN dbo.clientes AS c ON c.id = a.cliente_id
WHERE a.assunto = 'Validacao do atendimento CP5';
```

Mostre que `cliente_id` aponta para o ID anotado do cliente.

### 8.5 READ atendimento

Abra a listagem e o detalhe do atendimento. No SQL, repita a consulta do atendimento e compare assunto, descrição, status, data e cliente.

### 8.6 UPDATE atendimento

Edite o status para `Em andamento` e salve. No SQL:

```sql
SELECT id, assunto, status, cliente_id
FROM dbo.atendimentos
WHERE assunto = 'Validacao do atendimento CP5';
```

Mostre o mesmo ID com o novo status e a mesma relação.

### 8.7 DELETE atendimento

Antes de excluir, anote o ID do atendimento. Exclua pela tela. No SQL, troque `ID_ATENDIMENTO` pelo valor anotado:

```sql
SELECT id, assunto
FROM dbo.atendimentos
WHERE id = ID_ATENDIMENTO;
```

O resultado esperado é zero linhas. Em seguida, mostre que o cliente ainda existe:

```sql
SELECT id, nome, email
FROM dbo.clientes
WHERE email = 'bruno.cp5.558771@example.com';
```

### 8.8 DELETE cliente

Depois de remover o atendimento, exclua o cliente pela tela. No SQL:

```sql
SELECT id, nome, email
FROM dbo.clientes
WHERE email = 'bruno.cp5.558771@example.com';
```

O resultado esperado é zero linhas. Para demonstrar a regra da FK, durante a gravação também é possível criar outro atendimento, tentar excluir o cliente primeiro e mostrar a mensagem de bloqueio; depois remova o atendimento e conclua a exclusão.

## 9. Mostrar o Application Insights

Depois das operações, abra o recurso Application Insights no portal Azure e ajuste o intervalo de tempo para incluir os testes. Mostre:

1. requisições do Web App;
2. duração e resultado de uma requisição;
3. detalhe da transação;
4. dependência SQL associada à requisição;
5. Application Map ou a tela equivalente que mostre a relação entre aplicação e banco.

Se o painel estiver vazio, gere uma nova leitura ou alteração no aplicativo, aguarde alguns minutos e atualize o período. Não mostre apenas a existência do recurso ou a string de conexão: a evidência precisa conter coletas reais.

## 10. Ordem da gravação

Use esta ordem no vídeo:

1. Apresentar o problema, a equipe e o diagrama.
2. Mostrar o repositório e o conteúdo da pasta `scripts`.
3. Mostrar a assinatura ativa e a região.
4. Executar `01-criar-recursos.sh`.
5. Executar `02-aplicar-ddl.ps1` e mostrar as tabelas/FK.
6. Executar `03-configurar-aplicacao.sh`.
7. Compilar e executar o deploy com `04-deploy.sh`.
8. Abrir a URL pública do App Service.
9. Executar os oito passos CRUD, sempre com a consulta SQL logo depois.
10. Mostrar o bloqueio de exclusão por FK, se possível.
11. Mostrar Application Insights com requisições e dependências SQL.
12. Executar `05-verificar-recursos.sh` e encerrar com os links do repositório e do vídeo.

Fale durante a execução. Explique o que cada comando cria e o que cada consulta comprova. Faça uma pausa curta depois de cada resultado para que o professor consiga ler.

## 11. Problemas comuns

| Sintoma | Causa provável | Ação |
| --- | --- | --- |
| Assinatura `Disabled` | assinatura Azure inativa | reativar ou solicitar outra assinatura; não continuar o deploy |
| Nome já existe | nomes globais do Azure | alterar `PROJECT_NAME` e repetir em uma configuração limpa |
| F1 indisponível | quota ou região | escolher região permitida; não trocar para plano pago |
| Firewall bloqueia SQL | IP mudou | obter novo `curl -4 ifconfig.me` e atualizar apenas a regra desse IP |
| `Invoke-Sqlcmd` não encontrado | módulo SQL ausente | usar PowerShell do Cloud Shell ou instalar a ferramenta indicada pelo professor |
| App retorna 500 após deploy | variável de banco, DDL ou credencial incorreta | revisar script 02/03 e logs; não mudar `ddl-auto=validate` |
| App abre, mas não lista dados | banco vazio ou conexão incorreta | conferir URL, banco selecionado e tabelas no Azure SQL |
| Insights sem dados | intervalo curto ou reinício recente | gerar nova requisição, aguardar e ampliar o intervalo |
| Maven falha no Cloud Shell | Maven/JDK não disponíveis | compilar no PC e usar `04-deploy.sh --jar` |

## 12. Fechamento antes do envio

- [ ] Repositório público ou compartilhado com o professor.
- [ ] README sem placeholders de links.
- [ ] URL Azure abre fora da sessão do autor.
- [ ] DDL e scripts estão na pasta `scripts`.
- [ ] Vídeo tem pelo menos 720p e narração.
- [ ] Cada operação tem consulta SQL imediatamente depois.
- [ ] Application Insights mostra requisições e dependências SQL.
- [ ] Nenhuma senha, token ou string de conexão aparece.
- [ ] PDF segue exatamente `<nome_grupo>_webapp.pdf`.
- [ ] O Teams recebe somente o PDF pelo representante.

Se qualquer caixa permanecer desmarcada, não finalize a entrega.
