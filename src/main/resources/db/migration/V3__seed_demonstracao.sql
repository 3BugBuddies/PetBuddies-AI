-- =============================================================================
-- V3 — SEED DE DEMONSTRAÇÃO: SÓ AS LINHAS DE T_PB_USUARIO (decisão D-2.1)
-- =============================================================================
-- Um vet e um tutor, para a banca entrar. **Cada serviço semeia o que é dele:**
-- clínica, veterinário e responsável são semeados por migration do Entity
-- Framework, no .NET. Este arquivo não escreve em tabela de outro serviço.
--
-- ORDEM ENTRE OS DOIS SERVIÇOS
--   O V2 já declara FK_USUARIO_VETERINARIO e FK_USUARIO_RESPONSAVEL, então
--   inserir usuário SEMPRE exigiu que as linhas do .NET existissem — este
--   arquivo não acrescenta dependência nenhuma, só herda a que o V2 criou.
--   Se o seed do .NET ainda não rodou, o INSERT falha por FK, e o modo de falha
--   é o mesmo do V2: a migration fica gravada como falha no histórico do
--   Flyway e trava as subidas seguintes até um `flyway repair`.
--
-- AS SENHAS
--   As duas são a mesma senha de demonstração, cifrada em BCrypt (cost 10, 60
--   caracteres — cabe no VARCHAR2(100) de DS_SENHA_HASH). O valor em claro está
--   no corpo do PR: é dado de demonstração, e a banca precisa dele para entrar.
--   Os hashes foram gerados por uma classe descartável, que não é versionada.
--
-- IDS DE VÍNCULO
--   ID_VETERINARIO = 1 e ID_RESPONSAVEL = 1 são os ids que o seed do .NET
--   (PR-N4) grava. Se lá mudarem, mudam aqui — é o único acoplamento do
--   arquivo.
--
-- ID_USUARIO não é declarado: a coluna é IDENTITY (ADR s3-21).
-- =============================================================================

-- --- o vet ------------------------------------------------------------------
INSERT INTO T_PB_USUARIO (DS_LOGIN, DS_SENHA_HASH, TP_PERFIL, ID_VETERINARIO, AT_ATIVO)
VALUES ('ana@clinica.com',
        '$2a$10$v20f4a3MJrUHeusflIz/Y.JaUxfxFpAa18BztNQEuaNW8nXOaQnou',
        'VET', 1, 1);

-- --- o tutor ----------------------------------------------------------------
INSERT INTO T_PB_USUARIO (DS_LOGIN, DS_SENHA_HASH, TP_PERFIL, ID_RESPONSAVEL, AT_ATIVO)
VALUES ('maria@email.com',
        '$2a$10$m0xXaZ6pPj7kFwdkruEx4u.wuEPnPL05ZUVD9Sx2KEtLgdTi8fwUK',
        'TUTOR', 1, 1);
