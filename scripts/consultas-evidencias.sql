-- Execute no Azure SQL após operar os formulários da aplicação publicada.
-- Use dados fictícios de demonstração e substitua os IDs pelos IDs da gravação.

-- Identifica o banco e demonstra as tabelas criadas pelo ddl.sql.
SELECT DB_NAME() AS banco;
SELECT TABLE_SCHEMA, TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME IN ('clientes', 'atendimentos');

-- Confere o cliente criado/alterado e o atendimento associado a ele.
SELECT id, nome, email, telefone FROM dbo.clientes ORDER BY id;
SELECT a.id, a.assunto, a.descricao, a.status, a.data_abertura,
       a.cliente_id, c.nome AS cliente
FROM dbo.atendimentos AS a
INNER JOIN dbo.clientes AS c ON c.id = a.cliente_id
ORDER BY a.id;

-- Registre o ID antes de excluir o atendimento pelo frontend.
-- Depois da exclusão, esta consulta deve retornar zero linhas.
DECLARE @AtendimentoExcluido BIGINT = 1; -- Troque pelo ID realmente excluído.
SELECT id, assunto, status FROM dbo.atendimentos WHERE id = @AtendimentoExcluido;

-- Repita para o cliente depois de remover seus atendimentos e excluí-lo pelo frontend.
DECLARE @ClienteExcluido BIGINT = 1; -- Troque pelo ID realmente excluído.
SELECT id, nome, email FROM dbo.clientes WHERE id = @ClienteExcluido;

-- Evidencia a FK que impede apagar um cliente enquanto existem atendimentos.
SELECT name, delete_referential_action_desc, update_referential_action_desc
FROM sys.foreign_keys
WHERE name = 'FK_atendimentos_clientes';
