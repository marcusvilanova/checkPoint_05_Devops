# Relatório de conferência — Check Point 5

## 1. Objetivo

Este relatório confere o projeto DimDim Atendimento contra o enunciado do 2º Checkpoint do 2º semestre, as penalidades oficiais do documento da disciplina e os problemas apontados na avaliação do Check Point 4.

O relatório separa o que já está implementado no código e no repositório do que ainda precisa ser comprovado com execução real na Azure, vídeo e PDF. A existência de scripts ou documentação não substitui a evidência solicitada pelo professor.

## 2. Identificação da entrega

| Item | Situação atual |
| --- | --- |
| Repositório | https://github.com/marcusvilanova/checkPoint_05_Devops |
| Branch publicada | `main` |
| Aplicação | DimDim Atendimento |
| Linguagem | Java 17 |
| Modelo web | Spring MVC com Thymeleaf e formulários HTML |
| Banco obrigatório | Azure SQL Database Basic |
| Monitoramento | Application Insights com agente Java do App Service |
| Grupo | Ainda precisa ser definido |
| Azure | Ainda não implantada; a assinatura disponível estava `Disabled` |
| Vídeo | Ainda não gravado |
| PDF final | Ainda não criado |

O commit publicado foi reescrito para usar a identidade pessoal do responsável pelo repositório. O README genérico e este relatório precisam estar incluídos no próximo commit caso ainda não tenham sido enviados depois da última revisão.

## 3. O que foi implementado

### Aplicação

- Aplicação Java 17 com Spring Boot 3.3.4.
- Frontend MVC renderizado com Thymeleaf.
- Cadastro, consulta, edição e exclusão de clientes.
- Cadastro, consulta, edição e exclusão de atendimentos.
- Validação de dados no servidor.
- Tratamento de páginas de erro 404 e 500.
- Regra de negócio que impede excluir cliente com atendimento associado.
- Dados de demonstração fictícios.
- Testes automatizados de controllers e services.

### Persistência

- Tabela `dbo.clientes`.
- Tabela `dbo.atendimentos`.
- Relação de um cliente para vários atendimentos.
- Chave estrangeira `FK_atendimentos_clientes`.
- Índice `IX_atendimentos_cliente_id`.
- DDL idempotente em `scripts/ddl.sql`.
- JPA configurado com `ddl-auto=validate`, deixando a criação do esquema sob responsabilidade do DDL entregue.

### Azure e deploy

- Script de criação do grupo de recursos, plano Linux F1, Web App Java 17, SQL Server lógico, Azure SQL Basic, firewall e Application Insights.
- Script PowerShell para executar o DDL com `Invoke-Sqlcmd`.
- Script para configurar as variáveis do Spring, a conexão JDBC criptografada e o Application Insights.
- Script de deploy com `az webapp deploy --type jar`.
- Script de verificação dos recursos sem exibir credenciais.
- Configuração privada separada em `scripts/config.local.sh`, ignorada pelo Git.

### Documentação e arquitetura

- Descrição da solução no README.
- How to amplo no README, destinado a qualquer operador.
- Mapa operacional dos scripts.
- Desenho macro em PNG e SVG, mostrando navegador, GitHub, App Service, Azure SQL e Application Insights.
- Roteiro das evidências do vídeo.
- Arquivo separado com orientações operacionais detalhadas para a execução da gravação.

## 4. Conferência contra o enunciado

| Exigência | Evidência preparada | Situação |
| --- | --- | --- |
| Aplicação Java ou .NET | `pom.xml` e `src/main/java` | Atendido no código |
| Não ser a Sprint 3 | Domínio próprio DimDim Atendimento, com clientes e atendimentos | Atendido pela solução; comprovar no vídeo/apresentação |
| Deploy automatizado aprendido em aula | `scripts/04-deploy.sh` usando Azure CLI e `az webapp deploy --type jar` | Atendido no repositório; execução Azure pendente |
| Application Insights | `scripts/03-configurar-aplicacao.sh` e arquitetura | Configuração preparada; coletas reais pendentes |
| Banco PaaS não containerizado | Azure SQL Database Basic no `scripts/01-criar-recursos.sh` | Script preparado; recurso ainda não criado |
| Pelo menos duas tabelas relacionadas | `scripts/ddl.sql`, duas entidades e chave estrangeira | Atendido no código/DDL |
| CRUD em cada tabela | Controllers, services, templates e testes para clientes e atendimentos | Implementado; execução completa pendente |
| Frontend, não API | Páginas Thymeleaf e formulários MVC | Atendido |
| JSON GET/POST/PUT/DELETE | Não aplicável, pois o projeto não é uma API | Justificado no README |
| Projeto no GitHub | Repositório público com fonte, scripts e documentação | Atendido; revisar publicação das últimas alterações |
| How to completo | Tutorial de implantação no README | Atendido no conteúdo; confirmar que a última revisão foi publicada |
| DDL na pasta `scripts` | `scripts/ddl.sql` | Atendido |
| Scripts CLI na pasta `scripts` | Scripts numerados de 01 a 05 | Atendido |
| Link do vídeo | Campo reservado no README | Pendente da gravação |
| PDF no padrão solicitado | Orientação no README e roteiro | Pendente da criação e upload |

## 5. Conferência das penalidades oficiais

| Penalidade | Como foi tratada | Situação antes da entrega |
| --- | --- | --- |
| Entrega da Sprint 3 | Projeto novo de atendimento, sem reutilizar o exercício de Sprint 3 | Atendido pela proposta; manter a demonstração do domínio próprio |
| Projeto somente em localhost | Scripts e roteiro exigem URL pública do Azure App Service | Pendente: não entregar antes do deploy público |
| Professor sem acesso ao repositório ou vídeo | Repositório público; vídeo ainda precisa ser publicado com acesso verificável | Parcial |
| Vídeo abaixo de 720p ou sem fala | Roteiro exige mínimo de 720p, microfone e explicação falada | Pendente da gravação |
| Dados sensíveis expostos | Configuração local ignorada, credenciais solicitadas em memória e mensagens sem segredos | Atendido no material; revisar telas antes da gravação |
| Entrega fora do padrão | README orienta PDF `<nome_grupo>_webapp.pdf` e envio único pelo representante no Teams | Pendente do PDF, nome do grupo e upload |
| Não mostrar cada operação no banco | Roteiro exige oito operações e consulta SQL imediatamente após cada uma | Pendente da execução filmada |
| Não entregar Application Insights | Script configura o agente e o roteiro exige requisições e dependências SQL reais | Pendente das coletas na Azure |
| Sem How to no GitHub | README possui pré-requisitos, configuração, scripts, deploy, CRUD e monitoramento | Atendido; publicar a última revisão |
| Sem DDL | `scripts/ddl.sql` cria as tabelas, FK e índice | Atendido |
| CRUD em apenas uma tabela | Código e roteiro cobrem clientes e atendimentos | Implementado; provar as duas tabelas no vídeo |
| Sem scripts CLI | Cinco scripts de operação e verificação em `scripts` | Atendido |
| Sem código fonte | Código Java, templates, CSS, testes e `pom.xml` | Atendido |
| Sem descrição da solução | Descrição funcional, técnica e de escopo no README | Atendido |
| Sem arquitetura ou arquitetura como fluxograma | PNG/SVG mostram componentes e conexões, sem sequência de passos | Atendido no repositório |
| Sem JSON de API | Não aplicável: a implementação é MVC com frontend HTML | Justificado |
| Sem Azure SQL | Script cria SQL Server e banco Basic; DDL e conexão JDBC estão preparados | Pendente da criação e evidência na Azure |

## 6. Conferência do feedback do Check Point 4

### PDF, ZIP, TXT e repositório

O README e o roteiro orientam que o código, DDL, scripts, arquitetura e tutorial permaneçam no GitHub. A entrega do Teams deve conter somente o PDF com nome do grupo, nomes completos, RMs e links. Não deve ser enviado ZIP, TXT ou repositório anexado.

O PDF ainda precisa ser produzido. Portanto, esse ponto está planejado, mas não concluído.

### Scripts de execução

O projeto possui scripts separados para criação Azure, DDL, configuração, deploy e verificação. O CP5 exige scripts CLI; a entrega não depende de `docker run`, porque a solução usa App Service com JAR e Azure SQL PaaS, sem Docker.

### Arquitetura

O desenho entregue é uma arquitetura macro com componentes e conexões: navegador, GitHub, App Service Java 17, Azure SQL e Application Insights. Ele não é um fluxograma de etapas e não repete o desenho do exercício anterior.

### Aplicação diferente do exercício de aula

A aplicação usa o domínio DimDim Atendimento, com as entidades `Cliente` e `Atendimento`, os nomes de tabela `clientes` e `atendimentos`, regras próprias de vínculo e telas próprias de CRUD. A implementação não usa os nomes ou o banco do exercício anterior.

### Linguagem exigida

O projeto é Java 17, com código fonte completo, `pom.xml`, templates, testes e empacotamento JAR.

## 7. Evidências que ainda faltam

Os itens abaixo são obrigatórios para poder afirmar que a entrega está completa:

1. Assinatura Azure em estado `Enabled` e com crédito disponível.
2. Execução dos scripts 01 a 05 na nuvem.
3. Azure SQL com as duas tabelas e a chave estrangeira.
4. Aplicação acessível por URL pública do App Service.
5. Oito operações CRUD no frontend, com consulta SQL imediatamente após cada operação.
6. Consulta demonstrando a relação `cliente_id`.
7. Application Insights com requisições e dependências SQL reais.
8. Vídeo narrado em pelo menos 720p.
9. Link do vídeo acessível ao professor.
10. Nome do grupo definido.
11. PDF `<nome_grupo>_webapp.pdf` contendo somente grupo, nomes/RMs e links.
12. Upload somente do PDF pelo representante no Teams.
13. Substituição dos três placeholders de links no README.

## 8. Checklist de liberação

- [ ] README genérico publicado no GitHub.
- [ ] Este relatório, se escolhido como artefato da equipe, publicado no GitHub.
- [ ] Nome do grupo definido em `docs/integrantes.md` e no PDF.
- [ ] Assinatura Azure ativa.
- [ ] Recursos criados na região permitida.
- [ ] DDL executado e estrutura conferida.
- [ ] Variáveis do Web App configuradas sem exibir valores privados.
- [ ] JAR publicado com `az webapp deploy --type jar`.
- [ ] URL pública testada fora da sessão do autor.
- [ ] Cliente criado, lido, alterado e excluído com consulta SQL após cada passo.
- [ ] Atendimento criado, lido, alterado e excluído com consulta SQL após cada passo.
- [ ] Application Insights mostra requisições e dependências SQL.
- [ ] Vídeo narrado, legível e com resolução mínima de 720p.
- [ ] GitHub e vídeo acessíveis ao professor.
- [ ] PDF com a nomenclatura correta e somente as informações exigidas.
- [ ] Apenas o PDF enviado no Teams pelo representante.

## 9. Conclusão

O repositório contém a estrutura técnica necessária para atender o CP5 e corrige os problemas apontados no CP4 relacionados a aplicação Java própria, Azure SQL, scripts, DDL, arquitetura, How to e organização da entrega. A nota máxima ainda não pode ser garantida porque a implantação Azure, as coletas do Application Insights, o vídeo e o PDF são evidências de execução que ainda não foram produzidas.

A entrega deve ser considerada pronta somente quando todos os itens da seção 8 estiverem marcados após conferência real.
