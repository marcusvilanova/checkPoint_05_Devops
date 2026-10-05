# Roteiro do vídeo de evidências — Check Point 5

Sequência de gravação da aplicação DimDim Atendimento, com clientes e atendimentos. A implantação e os testes na nuvem ainda estão pendentes. Confira os nomes definitivos dos recursos, caminhos, comandos e campos antes de gravar.

Bruno Ferreira é o responsável pela execução e pela gravação. O vídeo deve apresentar a execução completa do passo a passo: criação dos recursos Azure, aplicação do DDL, deploy automatizado, teste do aplicativo publicado, consulta ao Azure SQL após cada CRUD e monitoramento real no Application Insights. A resolução mínima é **720p**, com explicação falada e conteúdo legível.

## Preparação

- [ ] Confirmar grupo, nomes completos, RMs, assinatura, região e identificadores finais dos recursos.
- [ ] Conferir que README, scripts, DDL e comandos de gravação correspondem ao código final.
- [ ] Preparar uma execução desde a criação dos recursos, preservando recursos de outros trabalhos.
- [ ] Preparar navegador, terminal e cliente SQL; fechar abas e saídas com credenciais ou outros segredos.
- [ ] Configurar a gravação para no mínimo 720p e testar microfone e legibilidade.
- [ ] Definir uma descrição única para os registros de demonstração, como `CP5 VIDEO CLIENTE` e `CP5 VIDEO ATENDIMENTO`.
- [ ] Preparar uma conexão ao Azure SQL usando valores privados; mostrar apenas servidor/banco e resultados, sem expor usuário de acesso, senha ou tokens.

## 1. Apresentação e repositório

Apresentar oralmente o grupo e o problema: registrar clientes da DimDim e acompanhar seus atendimentos. Explicar que um cliente pode possuir vários atendimentos e que cada atendimento pertence a um cliente.

Mostrar o GitHub, a descrição da solução, a estrutura de código, a pasta `scripts`, o DDL e o tutorial do README.

Mostrar o diagrama macro da arquitetura e explicar seus componentes: navegador, Azure App Service, plano de serviço, Azure SQL Database e Application Insights. Apontar as conexões usadas para acesso Web, persistência e telemetria, além da implantação pelo método escolhido.

## 2. Criação dos recursos Azure

Executar o tutorial e os scripts completos no ambiente indicado pelo README. Mostrar a criação do grupo de recursos, plano de serviço, Web App, SQL Server lógico, Azure SQL Database e Application Insights, na ordem definida pelos scripts.

Explicar que o banco utiliza um serviço PaaS Azure SQL, sem container. Mostrar os recursos criados na assinatura e a região escolhida. Configurar acesso ao banco, variáveis da aplicação e conexão com o Application Insights sem mostrar credenciais.

Não substituir a execução dos comandos por telas de recursos previamente existentes: o enunciado exige que o vídeo inclua a criação dos recursos.

## 3. DDL e relacionamento

Executar o arquivo DDL da pasta `scripts` no Azure SQL. Mostrar a definição das duas tabelas e a chave estrangeira que vincula atendimentos a clientes. Conferir que as tabelas existem antes de começar os testes.

As consultas abaixo pressupõem os nomes finais `dbo.clientes` e `dbo.atendimentos`. Se o DDL final adotar outros nomes, ajustar as consultas e este roteiro antes de gravar.

```sql
SELECT * FROM dbo.clientes;
SELECT * FROM dbo.atendimentos;
```

Explicar a coluna de relacionamento usando o nome exato definido no DDL. Depois da criação do atendimento, conferir que o valor persistido nessa coluna corresponde ao identificador do cliente criado pelo aplicativo.

## 4. Build e deploy automatizado

Executar o build conforme o README e mostrar seu resultado. Realizar o deploy pelo método documentado no projeto: **Azure CLI + `az webapp deploy`**, executando `scripts/04-deploy.sh`. Mostrar o envio do JAR e a conclusão do deploy. Abrir a URL pública do Azure App Service no navegador e mostrar o endereço durante os testes para comprovar que a aplicação executa na nuvem.

## 5. Oito operações CRUD com persistência

Executar as operações nesta ordem para respeitar o relacionamento. Após **cada operação**, alternar imediatamente do aplicativo para o cliente SQL, executar a consulta e explicar o resultado. Não agrupar todas as consultas apenas no fim do vídeo.

| Passo | Operação no aplicativo publicado | Consulta imediata no Azure SQL e evidência |
|---|---|---|
| 1 | **CREATE cliente:** cadastrar o cliente de demonstração com dados identificáveis. | `SELECT * FROM dbo.clientes;` Mostrar o novo registro e anotar seu identificador. |
| 2 | **READ cliente:** abrir a consulta/listagem ou detalhes do cliente recém-criado. | `SELECT * FROM dbo.clientes;` Comparar identificador e campos da tela com os dados persistidos. |
| 3 | **UPDATE cliente:** alterar um campo de forma visível, como o nome ou contato. | `SELECT * FROM dbo.clientes;` Mostrar o mesmo identificador com o campo atualizado. |
| 4 | **CREATE atendimento:** cadastrar um atendimento selecionando o cliente acima. | `SELECT * FROM dbo.atendimentos;` Mostrar o novo registro, seu identificador e o identificador do cliente. Consultar também `dbo.clientes` para comparar a relação. |
| 5 | **READ atendimento:** abrir a consulta/listagem ou detalhes do atendimento. | `SELECT * FROM dbo.atendimentos;` Comparar os dados da tela com o registro e sua associação ao cliente. |
| 6 | **UPDATE atendimento:** alterar um campo previsto pela aplicação, como descrição ou situação. | `SELECT * FROM dbo.atendimentos;` Mostrar o mesmo identificador com o campo atualizado e a relação preservada. |
| 7 | **DELETE atendimento:** excluir o atendimento de demonstração. | `SELECT * FROM dbo.atendimentos;` Mostrar a ausência desse identificador; consultar `dbo.clientes` e confirmar que o cliente permanece. |
| 8 | **DELETE cliente:** excluir o cliente, depois de remover o atendimento. | `SELECT * FROM dbo.clientes;` Mostrar a ausência do identificador do cliente; conferir a ausência do atendimento em `dbo.atendimentos`. |

Em um banco com outros registros, usar um filtro com o identificador real para tornar a conferência clara. O nome da coluna precisa corresponder ao DDL final. Não introduzir exclusões de registros alheios à demonstração.

Durante a narração, explicitar o que se espera antes de executar cada operação e o que o resultado SQL comprova. Nas leituras, mostrar a consulta do aplicativo e comparar com o SQL; nas exclusões, demonstrar que o registro desapareceu. Não realizar os CRUDs somente por SQL: as operações precisam passar pelo aplicativo.

## 6. Monitoramento do aplicativo e das operações SQL

Abrir o Application Insights associado ao Web App. Selecionar um intervalo de tempo que inclua as operações recém-executadas e atualizar a visualização quando necessário para que as coletas apareçam.

- [ ] Mostrar requisições reais do aplicativo geradas pelos testes, com horário, operação e resultado.
- [ ] Abrir o detalhe de uma requisição que acesse o banco e mostrar sua dependência SQL associada.
- [ ] Mostrar destino, duração e resultado da dependência, conforme disponíveis no portal.
- [ ] Explicar a relação entre requisição Web, chamada ao Azure SQL e resultado da operação demonstrada.
- [ ] Mostrar coletas suficientes para evidenciar as operações do app e o acesso ao banco.

Os nomes das telas dependem da interface do portal; usar as visualizações disponíveis de requisições, pesquisa de transações e detalhes da transação. O requisito é mostrar **dados coletados**, incluindo uma dependência SQL real. Apenas mostrar recurso criado, string de conexão ou painel vazio não conclui essa etapa.

Se as dependências SQL não aparecerem, corrigir a instrumentação antes de finalizar a gravação. Não apresentar somente as consultas diretas do cliente SQL como prova de telemetria do aplicativo: a dependência exibida precisa estar associada a uma requisição da aplicação.

## 7. Acesso e artefato final

Mostrar o repositório completo e o link do vídeo no README, depois que a gravação estiver publicada. Confirmar que o professor poderá abrir ambos; conferir em uma sessão sem autenticação quando a publicação for pública, ou verificar o acesso concedido quando houver restrição.

Preparar e conferir o PDF final chamado **`<nome_grupo>_webapp.pdf`**. Seu conteúdo deve ser somente:

- Nome do grupo.
- RM e nome completo de cada integrante.
- Link do GitHub.
- Link do vídeo.

Não inserir arquitetura, tutorial, capturas de tela, código ou anexos no PDF. Esses materiais ficam no GitHub. O representante deve enviar somente o PDF na tarefa da turma no Teams.

## Conferência da gravação pronta

- [ ] Criação dos recursos em nuvem aparece na gravação.
- [ ] DDL e relacionamento das duas tabelas aparecem.
- [ ] Build e deploy automatizado aparecem.
- [ ] Endereço do aplicativo em nuvem fica identificável durante os testes.
- [ ] As oito operações aparecem com consulta SQL após cada uma.
- [ ] A chave estrangeira e os registros relacionados são demonstrados.
- [ ] Application Insights mostra requisições reais e dependências SQL associadas.
- [ ] O vídeo tem no mínimo 720p, narração audível e textos legíveis.
- [ ] Credenciais, tokens e demais segredos não aparecem.
- [ ] Links GitHub/vídeo funcionam para o professor.
- [ ] Nome, integrantes, RMs e links do PDF foram confirmados.
- [ ] O representante enviou somente o PDF com a nomenclatura exigida.

Marcar os itens apenas após assistir à gravação final, testar os links e conferir o PDF. Um ensaio ou roteiro preparado não constitui evidência de conclusão.
