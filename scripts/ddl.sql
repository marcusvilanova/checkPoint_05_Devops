-- Execute no banco Azure SQL escolhido para o projeto, antes de iniciar a aplicação.
-- O script é idempotente: não apaga tabelas nem recria dados existentes.
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.clientes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.clientes (
        id BIGINT IDENTITY(1,1) NOT NULL,
        nome VARCHAR(150) NOT NULL,
        email VARCHAR(150) NOT NULL,
        telefone VARCHAR(20) NULL,
        CONSTRAINT PK_clientes PRIMARY KEY (id),
        CONSTRAINT CK_clientes_nome CHECK (LEN(LTRIM(RTRIM(nome))) > 0),
        CONSTRAINT CK_clientes_email CHECK (LEN(LTRIM(RTRIM(email))) > 0)
    );
END;

IF OBJECT_ID(N'dbo.atendimentos', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.atendimentos (
        id BIGINT IDENTITY(1,1) NOT NULL,
        assunto VARCHAR(150) NOT NULL,
        descricao VARCHAR(1000) NOT NULL,
        status VARCHAR(20) NOT NULL,
        data_abertura DATE NOT NULL,
        cliente_id BIGINT NOT NULL,
        CONSTRAINT PK_atendimentos PRIMARY KEY (id),
        CONSTRAINT CK_atendimentos_assunto CHECK (LEN(LTRIM(RTRIM(assunto))) > 0),
        CONSTRAINT CK_atendimentos_descricao CHECK (LEN(LTRIM(RTRIM(descricao))) > 0),
        CONSTRAINT CK_atendimentos_status CHECK (status IN ('ABERTO', 'EM_ANDAMENTO', 'CONCLUIDO')),
        CONSTRAINT FK_atendimentos_clientes FOREIGN KEY (cliente_id)
            REFERENCES dbo.clientes(id) ON DELETE NO ACTION ON UPDATE NO ACTION
    );
END;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.atendimentos') AND name = N'IX_atendimentos_cliente_id'
)
BEGIN
    CREATE INDEX IX_atendimentos_cliente_id ON dbo.atendimentos(cliente_id);
END;

COMMIT TRANSACTION;
