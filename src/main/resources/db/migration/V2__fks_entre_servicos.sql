-- =============================================================================
-- V2 — AS DEZ CHAVES QUE CRUZAM SERVIÇO, E NADA MAIS (ADR s3-23)
-- =============================================================================
-- Saem de uma das oito tabelas do cuidado (Java) e apontam para uma das onze do
-- registro (.NET), no MESMO schema Oracle. O cuidado aponta para o registro; o
-- contrário nunca acontece.
--
-- POR QUE ELAS ESTÃO NUM ARQUIVO SEPARADO
--   Nenhum ORM cria estas chaves: o Hibernate não enxerga as tabelas do .NET e
--   o Entity Framework não enxerga as do Java. Se estivessem no V1, o baseline
--   do cuidado passaria a exigir as onze tabelas do registro já criadas, e o
--   Java não poderia subir antes do .NET migrar.
--   Com o split, o V1 e a migration do EF são INDEPENDENTES — rodam em qualquer
--   ordem, ou em paralelo. Só este arquivo é ordenado, e ele é o último de
--   qualquer jeito.
--
-- CONSEQUÊNCIA DE ORQUESTRAÇÃO
--   Enquanto a migration rodar na subida da aplicação, este arquivo obriga o
--   Java a subir DEPOIS do .NET — `depends_on` no compose. Isso some quando o
--   CI/CD da Sprint 4 transformar a migration em passo de deploy: as migrations
--   não mudam, só quem as executa.
--
-- ASSIMETRIA DE PRECISÃO, deliberada
--   A coluna de origem é NUMBER(19) (o `Long` do Java) e a de destino é
--   NUMBER(10) (o `int` do .NET). Cada lado declara o que o próprio ORM gera, e
--   Oracle aceita a referência: a FK exige o mesmo tipo, não a mesma precisão.
--
-- Fonte: `database/src/01_ddl.sql`, bloco "AS DEZ QUE CRUZAM SERVIÇO".
-- =============================================================================

-- --- T_PB_USUARIO → a identidade do registro (2) ------------------------------
ALTER TABLE T_PB_USUARIO ADD CONSTRAINT FK_USUARIO_VETERINARIO
    FOREIGN KEY (ID_VETERINARIO) REFERENCES T_PB_VETERINARIO (ID_VETERINARIO);
ALTER TABLE T_PB_USUARIO ADD CONSTRAINT FK_USUARIO_RESPONSAVEL
    FOREIGN KEY (ID_RESPONSAVEL) REFERENCES T_PB_RESPONSAVEL (ID_RESPONSAVEL);

-- --- T_PB_PLANO_CUIDADO_ANIMAL → o pet e o atendimento (2) --------------------
ALTER TABLE T_PB_PLANO_CUIDADO_ANIMAL ADD CONSTRAINT FK_PLANO_ANIMAL
    FOREIGN KEY (ID_ANIMAL) REFERENCES T_PB_ANIMAL (ID_ANIMAL);
ALTER TABLE T_PB_PLANO_CUIDADO_ANIMAL ADD CONSTRAINT FK_PLANO_CONSULTA
    FOREIGN KEY (ID_CONSULTA) REFERENCES T_PB_CONSULTA (ID_CONSULTA);

-- --- T_PB_EVENTO_PLANO → o ato assinado e o fato clínico (2) ------------------
ALTER TABLE T_PB_EVENTO_PLANO ADD CONSTRAINT FK_EVPLANO_PRESCRICAO
    FOREIGN KEY (ID_PRESCRICAO) REFERENCES T_PB_PRESCRICAO (ID_PRESCRICAO);
ALTER TABLE T_PB_EVENTO_PLANO ADD CONSTRAINT FK_EVPLANO_PROCEDIMENTO
    FOREIGN KEY (ID_PROCEDIMENTO) REFERENCES T_PB_PROCEDIMENTO (ID_PROCEDIMENTO);

-- --- as quatro que o cluster do check-in acrescentou ao mudar de lado (08/09) --
ALTER TABLE T_PB_CHECKIN_TRATAMENTO ADD CONSTRAINT FK_CKTRAT_ANIMAL
    FOREIGN KEY (ID_ANIMAL) REFERENCES T_PB_ANIMAL (ID_ANIMAL);
ALTER TABLE T_PB_CHECKIN_EXTRACAO ADD CONSTRAINT FK_CKEXTR_CONDICAO
    FOREIGN KEY (ID_CONDICAO_CLINICA) REFERENCES T_PB_CONDICAO_CLINICA (ID_CONDICAO_CLINICA);
ALTER TABLE T_PB_CHECKIN_RESULTADO ADD CONSTRAINT FK_CKRES_PRESCRICAO
    FOREIGN KEY (ID_PRESCRICAO) REFERENCES T_PB_PRESCRICAO (ID_PRESCRICAO);
ALTER TABLE T_PB_CHECKIN_RESULTADO ADD CONSTRAINT FK_CKRES_REGRA
    FOREIGN KEY (ID_REGRA_APLICADA) REFERENCES T_PB_REGRA_PRESCRICAO (ID_REGRA_PRESCRICAO);
