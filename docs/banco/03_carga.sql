-- =============================================================================
-- 1. T_PB_CLINICA - a raiz de tudo
-- =============================================================================
INSERT INTO T_PB_CLINICA (ID_CLINICA, NM_NOME_CLINICA, NR_CNPJ, TL_TELEFONE, EM_EMAIL) VALUES
    (1, 'PetBuddies Clínica Centro',     '12345678000101', '1133334444', 'centro@petbuddies.com.br');
INSERT INTO T_PB_CLINICA (ID_CLINICA, NM_NOME_CLINICA, NR_CNPJ, TL_TELEFONE, EM_EMAIL) VALUES
    (2, 'PetBuddies Clínica Zona Sul',   '12345678000202', '1133335555', 'zonasul@petbuddies.com.br');
INSERT INTO T_PB_CLINICA (ID_CLINICA, NM_NOME_CLINICA, NR_CNPJ, TL_TELEFONE, EM_EMAIL) VALUES
    (3, 'Clínica Veterinária Amigo Fiel','22345678000103', '1133336666', 'contato@amigofiel.com.br');
INSERT INTO T_PB_CLINICA (ID_CLINICA, NM_NOME_CLINICA, NR_CNPJ, TL_TELEFONE, EM_EMAIL) VALUES
    (4, 'Clínica Pata Amiga',            '32345678000104', '1133337777', 'contato@pataamiga.com.br');
INSERT INTO T_PB_CLINICA (ID_CLINICA, NM_NOME_CLINICA, NR_CNPJ, TL_TELEFONE, EM_EMAIL) VALUES
    (5, 'Hospital Veterinário Vida',     '42345678000105', '1133338888', 'contato@hvvida.com.br');


-- =============================================================================
-- 2. T_PB_VETERINARIO - quem assina
-- =============================================================================
INSERT INTO T_PB_VETERINARIO (ID_VETERINARIO, NM_NOME_VETERINARIO, NR_CRMV, EM_EMAIL, AT_ATIVO, ID_CLINICA) VALUES
    (1, 'Ana Beatriz Moraes',   'SP-12345', 'ana.moraes@petbuddies.com.br',   1, 1);
INSERT INTO T_PB_VETERINARIO (ID_VETERINARIO, NM_NOME_VETERINARIO, NR_CRMV, EM_EMAIL, AT_ATIVO, ID_CLINICA) VALUES
    (2, 'Carlos Eduardo Lima',  'SP-23456', 'carlos.lima@petbuddies.com.br',  1, 1);
INSERT INTO T_PB_VETERINARIO (ID_VETERINARIO, NM_NOME_VETERINARIO, NR_CRMV, EM_EMAIL, AT_ATIVO, ID_CLINICA) VALUES
    (3, 'Daniela Ferreira',     'SP-34567', 'daniela.ferreira@petbuddies.com.br', 1, 2);
INSERT INTO T_PB_VETERINARIO (ID_VETERINARIO, NM_NOME_VETERINARIO, NR_CRMV, EM_EMAIL, AT_ATIVO, ID_CLINICA) VALUES
    (4, 'Eduardo Nunes',        'SP-45678', 'eduardo.nunes@amigofiel.com.br', 1, 3);
INSERT INTO T_PB_VETERINARIO (ID_VETERINARIO, NM_NOME_VETERINARIO, NR_CRMV, EM_EMAIL, AT_ATIVO, ID_CLINICA) VALUES
    (5, 'Fernanda Rocha',       'SP-56789', 'fernanda.rocha@pataamiga.com.br', 0, 4);


-- =============================================================================
-- 3. T_PB_RESPONSAVEL - o tutor
-- =============================================================================
INSERT INTO T_PB_RESPONSAVEL (ID_RESPONSAVEL, NM_NOME_RESPONSAVEL, TL_TELEFONE, EM_EMAIL) VALUES
    (1, 'Gabriel Nogueira',     '11987650001', 'gabriel.nogueira@email.com');
INSERT INTO T_PB_RESPONSAVEL (ID_RESPONSAVEL, NM_NOME_RESPONSAVEL, TL_TELEFONE, EM_EMAIL) VALUES
    (2, 'Helena Cardoso',       '11987650002', 'helena.cardoso@email.com');
INSERT INTO T_PB_RESPONSAVEL (ID_RESPONSAVEL, NM_NOME_RESPONSAVEL, TL_TELEFONE, EM_EMAIL) VALUES
    (3, 'Igor Bertolucci',      '11987650003', 'igor.bertolucci@email.com');
INSERT INTO T_PB_RESPONSAVEL (ID_RESPONSAVEL, NM_NOME_RESPONSAVEL, TL_TELEFONE, EM_EMAIL) VALUES
    (4, 'Juliana Prado',        '11987650004', 'juliana.prado@email.com');
INSERT INTO T_PB_RESPONSAVEL (ID_RESPONSAVEL, NM_NOME_RESPONSAVEL, TL_TELEFONE, EM_EMAIL) VALUES
    (5, 'Lucas Andrade',        '11987650005', 'lucas.andrade@email.com');


-- =============================================================================
-- 4. T_PB_ANIMAL - o pet, já com espécie, raça e porte absorvidos 
-- =============================================================================
INSERT INTO T_PB_ANIMAL (ID_ANIMAL, NM_NOME_ANIMAL, ES_ESPECIE, RC_RACA, PT_PORTE, SX_SEXO,
                         DT_DATA_NASCIMENTO, NR_PESO, CN_CONDICAO_CRONICA, CT_CASTRADO,
                         OB_ALERGIA, OB_OBSERVACOES, ID_RESPONSAVEL) VALUES
    (1, 'Thor', 'CACHORRO', 'Labrador',        'GRANDE',  'MACHO', DATE '2021-03-14', 32.50, 0, 1,
        'Alergia a frango', 'Ansioso em dias de tempestade', 1);
INSERT INTO T_PB_ANIMAL (ID_ANIMAL, NM_NOME_ANIMAL, ES_ESPECIE, RC_RACA, PT_PORTE, SX_SEXO,
                         DT_DATA_NASCIMENTO, NR_PESO, CN_CONDICAO_CRONICA, CT_CASTRADO,
                         OB_ALERGIA, OB_OBSERVACOES, ID_RESPONSAVEL) VALUES
    (2, 'Mia',  'GATO',     'Siamês',          'PEQUENO', 'FEMEA', DATE '2022-07-02',  4.20, 0, 1,
        NULL, 'Não aceita transporte na caixa', 2);
INSERT INTO T_PB_ANIMAL (ID_ANIMAL, NM_NOME_ANIMAL, ES_ESPECIE, RC_RACA, PT_PORTE, SX_SEXO,
                         DT_DATA_NASCIMENTO, NR_PESO, CN_CONDICAO_CRONICA, CT_CASTRADO,
                         OB_ALERGIA, OB_OBSERVACOES, ID_RESPONSAVEL) VALUES
    (3, 'Bidu', 'CACHORRO', 'Sem raça definida','MEDIO',  'MACHO', DATE '2019-11-30', 18.00, 1, 1,
        NULL, 'Displasia coxofemoral leve', 3);
INSERT INTO T_PB_ANIMAL (ID_ANIMAL, NM_NOME_ANIMAL, ES_ESPECIE, RC_RACA, PT_PORTE, SX_SEXO,
                         DT_DATA_NASCIMENTO, NR_PESO, CN_CONDICAO_CRONICA, CT_CASTRADO,
                         OB_ALERGIA, OB_OBSERVACOES, ID_RESPONSAVEL) VALUES
    (4, 'Nina', 'GATO',     'Persa',           'PEQUENO', 'FEMEA', DATE '2020-01-20',  5.10, 1, 0,
        'Alergia a dipirona', 'Doença renal crônica em acompanhamento', 4);
INSERT INTO T_PB_ANIMAL (ID_ANIMAL, NM_NOME_ANIMAL, ES_ESPECIE, RC_RACA, PT_PORTE, SX_SEXO,
                         DT_DATA_NASCIMENTO, NR_PESO, CN_CONDICAO_CRONICA, CT_CASTRADO,
                         OB_ALERGIA, OB_OBSERVACOES, ID_RESPONSAVEL) VALUES
    (5, 'Loki', 'COELHO',   'Mini Lop',        'MINI',    'MACHO', DATE '2023-05-08',  1.80, 0, 0,
        NULL, NULL, 5);


-- =============================================================================
-- 5. T_PB_CONDICAO_CLINICA - o catálogo, todo assinado pela RT da clínica 
-- =============================================================================
INSERT INTO T_PB_CONDICAO_CLINICA (ID_CONDICAO_CLINICA, CD_CODIGO, DS_ROTULO, TP_DADO, TP_FONTE_VALOR,
                                   DS_UNIDADE, FL_CRITICA, AT_ATIVO, ID_CLINICA, ID_VETERINARIO_AUTOR) VALUES
    (1, 'VOMITO',      'O pet vomitou desde o último check-in?', 'BOOLEANO', 'RELATO', NULL,  0, 1, 1, 1);
INSERT INTO T_PB_CONDICAO_CLINICA (ID_CONDICAO_CLINICA, CD_CODIGO, DS_ROTULO, TP_DADO, TP_FONTE_VALOR,
                                   DS_UNIDADE, FL_CRITICA, AT_ATIVO, ID_CLINICA, ID_VETERINARIO_AUTOR) VALUES
    (2, 'DIARREIA',    'Como estavam as fezes?',                 'BOOLEANO', 'RELATO', NULL,  0, 1, 1, 1);
INSERT INTO T_PB_CONDICAO_CLINICA (ID_CONDICAO_CLINICA, CD_CODIGO, DS_ROTULO, TP_DADO, TP_FONTE_VALOR,
                                   DS_UNIDADE, FL_CRITICA, AT_ATIVO, ID_CLINICA, ID_VETERINARIO_AUTOR) VALUES
    (3, 'TEMPERATURA', 'Qual a temperatura medida?',             'NUMERICO', 'RELATO', 'C',   0, 1, 1, 1);
INSERT INTO T_PB_CONDICAO_CLINICA (ID_CONDICAO_CLINICA, CD_CODIGO, DS_ROTULO, TP_DADO, TP_FONTE_VALOR,
                                   DS_UNIDADE, FL_CRITICA, AT_ATIVO, ID_CLINICA, ID_VETERINARIO_AUTOR) VALUES
    (4, 'APETITE_BAIXO','O pet comeu normalmente?',              'BOOLEANO', 'RELATO', NULL,  0, 1, 1, 1);

INSERT INTO T_PB_CONDICAO_CLINICA (ID_CONDICAO_CLINICA, CD_CODIGO, DS_ROTULO, TP_DADO, TP_FONTE_VALOR,
                                   DS_UNIDADE, FL_CRITICA, AT_ATIVO, ID_CLINICA, ID_VETERINARIO_AUTOR) VALUES
    (5, 'CONVULSAO',   'O pet teve algum episódio de convulsão?','BOOLEANO', 'RELATO', NULL,  1, 1, 1, 1);
INSERT INTO T_PB_CONDICAO_CLINICA (ID_CONDICAO_CLINICA, CD_CODIGO, DS_ROTULO, TP_DADO, TP_FONTE_VALOR,
                                   DS_UNIDADE, FL_CRITICA, AT_ATIVO, ID_CLINICA, ID_VETERINARIO_AUTOR) VALUES
    (6, 'PESO',        'Qual o peso atual?',                     'NUMERICO', 'RELATO', 'kg',  0, 1, 1, 1);


-- =============================================================================
-- 6. T_PB_CONSULTA
-- =============================================================================
INSERT INTO T_PB_CONSULTA (ID_CONSULTA, TP_TIPO_CONSULTA, DH_DATA_HORA, ST_STATUS_CONSULTA,
                           OB_OBSERVACAO, MT_MOTIVO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (1, 'ROTINA',    TIMESTAMP '2026-08-28 09:00:00', 'REALIZADA', NULL, 'Vômito há dois dias', 1, 1);
INSERT INTO T_PB_CONSULTA (ID_CONSULTA, TP_TIPO_CONSULTA, DH_DATA_HORA, ST_STATUS_CONSULTA,
                           OB_OBSERVACAO, MT_MOTIVO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (2, 'ROTINA',    TIMESTAMP '2026-08-28 10:00:00', 'REALIZADA', NULL, 'Espirros frequentes', 2, 1);
INSERT INTO T_PB_CONSULTA (ID_CONSULTA, TP_TIPO_CONSULTA, DH_DATA_HORA, ST_STATUS_CONSULTA,
                           OB_OBSERVACAO, MT_MOTIVO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (3, 'RETORNO',   TIMESTAMP '2026-08-29 14:00:00', 'REALIZADA', 'Retorno da cirurgia', 'Revisão pós-operatória', 3, 2);
INSERT INTO T_PB_CONSULTA (ID_CONSULTA, TP_TIPO_CONSULTA, DH_DATA_HORA, ST_STATUS_CONSULTA,
                           OB_OBSERVACAO, MT_MOTIVO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (4, 'EXAME',     TIMESTAMP '2026-08-30 11:00:00', 'REALIZADA', NULL, 'Controle renal', 4, 3);
INSERT INTO T_PB_CONSULTA (ID_CONSULTA, TP_TIPO_CONSULTA, DH_DATA_HORA, ST_STATUS_CONSULTA,
                           OB_OBSERVACAO, MT_MOTIVO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (5, 'VACINACAO', TIMESTAMP '2026-09-15 09:30:00', 'AGENDADA',  NULL, 'Primeira dose', 5, 1);
-- A consulta de 2025 existe por um motivo específico: sem histórico com mais de
-- doze meses, a sugestão preventiva do ADR s3-24 cai SEMPRE no ramo "nunca fez",
-- e o ramo "vencida" — que é a razão de a âncora ULTIMA_REALIZACAO existir —
-- nunca é exercitado.
INSERT INTO T_PB_CONSULTA (ID_CONSULTA, TP_TIPO_CONSULTA, DH_DATA_HORA, ST_STATUS_CONSULTA,
                           OB_OBSERVACAO, MT_MOTIVO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (6, 'VACINACAO', TIMESTAMP '2025-08-20 10:00:00', 'REALIZADA', NULL, 'Vacinação anual', 2, 1);


-- =============================================================================
-- 7. T_PB_JANELA_ATENDIMENTO - slots de 30 min. ID_CONSULTA nulo = livre.
-- =============================================================================
INSERT INTO T_PB_JANELA_ATENDIMENTO (ID_JANELA_ATENDIMENTO, DH_DATA_HORA_INICIO, ID_VETERINARIO, ID_CONSULTA) VALUES
    (1, TIMESTAMP '2026-08-28 09:00:00', 1, 1);
INSERT INTO T_PB_JANELA_ATENDIMENTO (ID_JANELA_ATENDIMENTO, DH_DATA_HORA_INICIO, ID_VETERINARIO, ID_CONSULTA) VALUES
    (2, TIMESTAMP '2026-08-28 10:00:00', 1, 2);
INSERT INTO T_PB_JANELA_ATENDIMENTO (ID_JANELA_ATENDIMENTO, DH_DATA_HORA_INICIO, ID_VETERINARIO, ID_CONSULTA) VALUES
    (3, TIMESTAMP '2026-08-29 14:00:00', 2, 3);
INSERT INTO T_PB_JANELA_ATENDIMENTO (ID_JANELA_ATENDIMENTO, DH_DATA_HORA_INICIO, ID_VETERINARIO, ID_CONSULTA) VALUES
    (4, TIMESTAMP '2026-08-30 11:00:00', 3, 4);
INSERT INTO T_PB_JANELA_ATENDIMENTO (ID_JANELA_ATENDIMENTO, DH_DATA_HORA_INICIO, ID_VETERINARIO, ID_CONSULTA) VALUES
    (5, TIMESTAMP '2026-09-15 09:30:00', 1, 5);
-- Livre: é assim que a agenda oferece horário ao tutor.
INSERT INTO T_PB_JANELA_ATENDIMENTO (ID_JANELA_ATENDIMENTO, DH_DATA_HORA_INICIO, ID_VETERINARIO, ID_CONSULTA) VALUES
    (6, TIMESTAMP '2026-09-15 10:00:00', 1, NULL);
INSERT INTO T_PB_JANELA_ATENDIMENTO (ID_JANELA_ATENDIMENTO, DH_DATA_HORA_INICIO, ID_VETERINARIO, ID_CONSULTA) VALUES
    (7, TIMESTAMP '2025-08-20 10:00:00', 1, 6);


-- =============================================================================
-- 8. T_PB_REGISTRO_ATENDIMENTO - o que aconteceu na consulta
-- =============================================================================
INSERT INTO T_PB_REGISTRO_ATENDIMENTO (ID_REGISTRO_ATENDIMENTO, DT_DATA_ATENDIMENTO, AN_ANAMNESE,
                                       DG_DIAGNOSTICO, TR_TRATAMENTO, OB_OBSERVACAO,
                                       PR_PROXIMO_RETORNO, PR_PROXIMA_VACINA, ID_ANIMAL, ID_CONSULTA) VALUES
    (1, TIMESTAMP '2026-08-28 09:20:00', 'Tutor relata vômito há dois dias, sem sangue.',
        'Gastrite aguda', 'Antibiótico e analgésico por sete dias', NULL,
        DATE '2026-09-10', DATE '2026-11-01', 1, 1);
INSERT INTO T_PB_REGISTRO_ATENDIMENTO (ID_REGISTRO_ATENDIMENTO, DT_DATA_ATENDIMENTO, AN_ANAMNESE,
                                       DG_DIAGNOSTICO, TR_TRATAMENTO, OB_OBSERVACAO,
                                       PR_PROXIMO_RETORNO, PR_PROXIMA_VACINA, ID_ANIMAL, ID_CONSULTA) VALUES
    (2, TIMESTAMP '2026-08-28 10:25:00', 'Espirros frequentes há uma semana.',
        'Rinotraqueíte felina', 'Antibiótico por sete dias', NULL,
        DATE '2026-09-11', NULL, 2, 2);
INSERT INTO T_PB_REGISTRO_ATENDIMENTO (ID_REGISTRO_ATENDIMENTO, DT_DATA_ATENDIMENTO, AN_ANAMNESE,
                                       DG_DIAGNOSTICO, TR_TRATAMENTO, OB_OBSERVACAO,
                                       PR_PROXIMO_RETORNO, PR_PROXIMA_VACINA, ID_ANIMAL, ID_CONSULTA) VALUES
    (3, TIMESTAMP '2026-08-29 14:30:00', 'Retorno de cirurgia ortopédica.',
        'Pós-operatório sem intercorrência', 'Anti-inflamatório por cinco dias', 'Retirar pontos em 10 dias',
        DATE '2026-09-08', NULL, 3, 3);
INSERT INTO T_PB_REGISTRO_ATENDIMENTO (ID_REGISTRO_ATENDIMENTO, DT_DATA_ATENDIMENTO, AN_ANAMNESE,
                                       DG_DIAGNOSTICO, TR_TRATAMENTO, OB_OBSERVACAO,
                                       PR_PROXIMO_RETORNO, PR_PROXIMA_VACINA, ID_ANIMAL, ID_CONSULTA) VALUES
    (4, TIMESTAMP '2026-08-30 11:20:00', 'Acompanhamento de doença renal crônica.',
        'DRC estágio 2 estável', 'Corticoide em dose baixa', NULL,
        DATE '2026-09-30', NULL, 4, 4);
INSERT INTO T_PB_REGISTRO_ATENDIMENTO (ID_REGISTRO_ATENDIMENTO, DT_DATA_ATENDIMENTO, AN_ANAMNESE,
                                       DG_DIAGNOSTICO, TR_TRATAMENTO, OB_OBSERVACAO,
                                       PR_PROXIMO_RETORNO, PR_PROXIMA_VACINA, ID_ANIMAL, ID_CONSULTA) VALUES
    (5, TIMESTAMP '2026-08-30 15:00:00', 'Avaliação inicial de filhote.',
        'Animal hígido', 'Iniciar protocolo preventivo', NULL,
        NULL, DATE '2026-09-15', 5, 5);
INSERT INTO T_PB_REGISTRO_ATENDIMENTO (ID_REGISTRO_ATENDIMENTO, DT_DATA_ATENDIMENTO, AN_ANAMNESE,
                                       DG_DIAGNOSTICO, TR_TRATAMENTO, OB_OBSERVACAO,
                                       PR_PROXIMO_RETORNO, PR_PROXIMA_VACINA, ID_ANIMAL, ID_CONSULTA) VALUES
    (6, TIMESTAMP '2025-08-20 10:15:00', 'Vacinação anual de rotina.',
        'Animal hígido', 'Vacina aplicada sem intercorrência', NULL,
        NULL, DATE '2026-08-20', 2, 6);


-- =============================================================================
-- 9. T_PB_PROCEDIMENTO - o fato que baixa item da clínica no checklist
-- =============================================================================
INSERT INTO T_PB_PROCEDIMENTO (ID_PROCEDIMENTO, TP_TIPO_PROCEDIMENTO, NM_NOME, DS_DESCRICAO,
                               ST_STATUS_PROCEDIMENTO, DT_DATA_PREVISTA_INICIO, DT_DATA_PREVISTA_FIM,
                               ID_REGISTRO_ATENDIMENTO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (1, 'EXAME_LABORATORIAL', 'Hemograma completo', 'Coleta em jejum', 'REALIZADO',
        TIMESTAMP '2026-08-28 09:30:00', TIMESTAMP '2026-08-28 10:00:00', 1, 1, 1);
INSERT INTO T_PB_PROCEDIMENTO (ID_PROCEDIMENTO, TP_TIPO_PROCEDIMENTO, NM_NOME, DS_DESCRICAO,
                               ST_STATUS_PROCEDIMENTO, DT_DATA_PREVISTA_INICIO, DT_DATA_PREVISTA_FIM,
                               ID_REGISTRO_ATENDIMENTO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (2, 'EXAME_IMAGEM', 'Raio-X abdominal', NULL, 'REALIZADO',
        TIMESTAMP '2026-08-28 10:30:00', TIMESTAMP '2026-08-28 11:00:00', 2, 2, 1);
INSERT INTO T_PB_PROCEDIMENTO (ID_PROCEDIMENTO, TP_TIPO_PROCEDIMENTO, NM_NOME, DS_DESCRICAO,
                               ST_STATUS_PROCEDIMENTO, DT_DATA_PREVISTA_INICIO, DT_DATA_PREVISTA_FIM,
                               ID_REGISTRO_ATENDIMENTO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (3, 'CIRURGIA', 'Revisão de osteossíntese', 'Procedimento anterior', 'REALIZADO',
        TIMESTAMP '2026-08-29 15:00:00', TIMESTAMP '2026-08-29 16:30:00', 3, 3, 2);
INSERT INTO T_PB_PROCEDIMENTO (ID_PROCEDIMENTO, TP_TIPO_PROCEDIMENTO, NM_NOME, DS_DESCRICAO,
                               ST_STATUS_PROCEDIMENTO, DT_DATA_PREVISTA_INICIO, DT_DATA_PREVISTA_FIM,
                               ID_REGISTRO_ATENDIMENTO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (4, 'EXAME_LABORATORIAL', 'Ureia e creatinina', 'Controle renal', 'REALIZADO',
        TIMESTAMP '2026-08-30 11:30:00', TIMESTAMP '2026-08-30 12:00:00', 4, 4, 3);
INSERT INTO T_PB_PROCEDIMENTO (ID_PROCEDIMENTO, TP_TIPO_PROCEDIMENTO, NM_NOME, DS_DESCRICAO,
                               ST_STATUS_PROCEDIMENTO, DT_DATA_PREVISTA_INICIO, DT_DATA_PREVISTA_FIM,
                               ID_REGISTRO_ATENDIMENTO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (5, 'VACINACAO', 'Vacina V10 - primeira dose', NULL, 'PENDENTE',
        TIMESTAMP '2026-09-15 09:30:00', TIMESTAMP '2026-09-15 10:00:00', 5, 5, 1);

INSERT INTO T_PB_PROCEDIMENTO (ID_PROCEDIMENTO, TP_TIPO_PROCEDIMENTO, NM_NOME, DS_DESCRICAO,
                               ST_STATUS_PROCEDIMENTO, DT_DATA_PREVISTA_INICIO, DT_DATA_PREVISTA_FIM,
                               ID_REGISTRO_ATENDIMENTO, ID_ANIMAL, ID_VETERINARIO) VALUES
    (6, 'VACINACAO', 'Vacina quádrupla felina - anual', 'Aplicada em 2025', 'REALIZADO',
        TIMESTAMP '2025-08-20 10:15:00', TIMESTAMP '2025-08-20 10:30:00', 6, 2, 1);


-- =============================================================================
-- 10. T_PB_PRESCRICAO - o ato assinado. Imutável: AT_UPDATED_AT fica nulo.
-- =============================================================================
INSERT INTO T_PB_PRESCRICAO (ID_PRESCRICAO, NM_MEDICAMENTO, NR_DOSE_MIN, NR_DOSE_MAX, DS_UNIDADE,
                             NR_FREQUENCIA_DIA, NR_DURACAO_DIAS, DT_INICIO, TX_ORIENTACAO,
                             ID_ANIMAL, ID_VETERINARIO, ID_REGISTRO_ATENDIMENTO) VALUES
    (1, 'Amoxicilina',  0.250, 0.750, 'ml', 2, 7, DATE '2026-09-01',
        'Dar junto com a comida. Se vomitar, suspender a dose do dia.', 1, 1, 1);
INSERT INTO T_PB_PRESCRICAO (ID_PRESCRICAO, NM_MEDICAMENTO, NR_DOSE_MIN, NR_DOSE_MAX, DS_UNIDADE,
                             NR_FREQUENCIA_DIA, NR_DURACAO_DIAS, DT_INICIO, TX_ORIENTACAO,
                             ID_ANIMAL, ID_VETERINARIO, ID_REGISTRO_ATENDIMENTO) VALUES
    (2, 'Amoxicilina',  0.500, 1.000, 'ml', 2, 7, DATE '2026-09-01',
        'Dar pela manhã e à noite.', 2, 1, 2);
INSERT INTO T_PB_PRESCRICAO (ID_PRESCRICAO, NM_MEDICAMENTO, NR_DOSE_MIN, NR_DOSE_MAX, DS_UNIDADE,
                             NR_FREQUENCIA_DIA, NR_DURACAO_DIAS, DT_INICIO, TX_ORIENTACAO,
                             ID_ANIMAL, ID_VETERINARIO, ID_REGISTRO_ATENDIMENTO) VALUES
    (3, 'Dipirona',     0.500, 1.500, 'ml', 3, 5, DATE '2026-09-01',
        'Somente se houver sinal de dor ou febre.', 1, 1, 1);
INSERT INTO T_PB_PRESCRICAO (ID_PRESCRICAO, NM_MEDICAMENTO, NR_DOSE_MIN, NR_DOSE_MAX, DS_UNIDADE,
                             NR_FREQUENCIA_DIA, NR_DURACAO_DIAS, DT_INICIO, TX_ORIENTACAO,
                             ID_ANIMAL, ID_VETERINARIO, ID_REGISTRO_ATENDIMENTO) VALUES
    (4, 'Meloxicam',    0.250, 0.750, 'ml', 1, 5, DATE '2026-09-01',
        'Após a refeição. Reduzir se houver diarreia.', 3, 2, 3);
INSERT INTO T_PB_PRESCRICAO (ID_PRESCRICAO, NM_MEDICAMENTO, NR_DOSE_MIN, NR_DOSE_MAX, DS_UNIDADE,
                             NR_FREQUENCIA_DIA, NR_DURACAO_DIAS, DT_INICIO, TX_ORIENTACAO,
                             ID_ANIMAL, ID_VETERINARIO, ID_REGISTRO_ATENDIMENTO) VALUES
    (5, 'Prednisolona', 1.000, 3.000, 'mg', 1, 10, DATE '2026-09-01',
        'Não interromper sem orientação.', 4, 3, 4);


-- =============================================================================
-- 11. T_PB_REGRA_PRESCRICAO - condição → ação, com o rótulo congelado
-- =============================================================================
INSERT INTO T_PB_REGRA_PRESCRICAO (ID_REGRA_PRESCRICAO, ID_PRESCRICAO, ID_CONDICAO_CLINICA,
                                   DS_ROTULO_CONGELADO, TP_DADO_CONGELADO, TP_FONTE_VALOR_CONGELADA,
                                   TP_OPERADOR, NR_LIMITE, TP_ACAO_DOSE, NR_ORDEM) VALUES
    (1, 1, 1, 'O pet vomitou desde o último check-in?', 'BOOLEANO', 'RELATO',
        NULL, NULL, 'SUSPENDER', 1);
INSERT INTO T_PB_REGRA_PRESCRICAO (ID_REGRA_PRESCRICAO, ID_PRESCRICAO, ID_CONDICAO_CLINICA,
                                   DS_ROTULO_CONGELADO, TP_DADO_CONGELADO, TP_FONTE_VALOR_CONGELADA,
                                   TP_OPERADOR, NR_LIMITE, TP_ACAO_DOSE, NR_ORDEM) VALUES
    (2, 1, 3, 'Qual a temperatura medida?', 'NUMERICO', 'RELATO',
        'MAIOR_QUE', 39.500, 'DOSE_MAX', 2);
INSERT INTO T_PB_REGRA_PRESCRICAO (ID_REGRA_PRESCRICAO, ID_PRESCRICAO, ID_CONDICAO_CLINICA,
                                   DS_ROTULO_CONGELADO, TP_DADO_CONGELADO, TP_FONTE_VALOR_CONGELADA,
                                   TP_OPERADOR, NR_LIMITE, TP_ACAO_DOSE, NR_ORDEM) VALUES
    (3, 3, 5, 'O pet teve algum episódio de convulsão?', 'BOOLEANO', 'RELATO',
        NULL, NULL, 'ACIONAR_CLINICA', 1);
INSERT INTO T_PB_REGRA_PRESCRICAO (ID_REGRA_PRESCRICAO, ID_PRESCRICAO, ID_CONDICAO_CLINICA,
                                   DS_ROTULO_CONGELADO, TP_DADO_CONGELADO, TP_FONTE_VALOR_CONGELADA,
                                   TP_OPERADOR, NR_LIMITE, TP_ACAO_DOSE, NR_ORDEM) VALUES
    (4, 3, 3, 'Qual a temperatura medida?', 'NUMERICO', 'RELATO',
        'MAIOR_OU_IGUAL', 39.000, 'DOSE_MAX', 2);
INSERT INTO T_PB_REGRA_PRESCRICAO (ID_REGRA_PRESCRICAO, ID_PRESCRICAO, ID_CONDICAO_CLINICA,
                                   DS_ROTULO_CONGELADO, TP_DADO_CONGELADO, TP_FONTE_VALOR_CONGELADA,
                                   TP_OPERADOR, NR_LIMITE, TP_ACAO_DOSE, NR_ORDEM) VALUES
    (5, 2, 1, 'O pet vomitou desde o último check-in?', 'BOOLEANO', 'RELATO',
        NULL, NULL, 'SUSPENDER', 1);
INSERT INTO T_PB_REGRA_PRESCRICAO (ID_REGRA_PRESCRICAO, ID_PRESCRICAO, ID_CONDICAO_CLINICA,
                                   DS_ROTULO_CONGELADO, TP_DADO_CONGELADO, TP_FONTE_VALOR_CONGELADA,
                                   TP_OPERADOR, NR_LIMITE, TP_ACAO_DOSE, NR_ORDEM) VALUES
    (6, 4, 2, 'Como estavam as fezes?', 'BOOLEANO', 'RELATO',
        NULL, NULL, 'DOSE_MIN', 1);

-- =============================================================================
-- 12. T_PB_USUARIO - login único para os dois perfis
-- =============================================================================
INSERT INTO T_PB_USUARIO (ID_USUARIO, DS_LOGIN, DS_SENHA_HASH, TP_PERFIL, ID_VETERINARIO, ID_RESPONSAVEL, AT_ATIVO) VALUES
    (1, 'ana.moraes@petbuddies.com.br',   '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4HhSeedTecoRuKa2Vh0LxUWQ9Wsi', 'VET',   1,    NULL, 1);
INSERT INTO T_PB_USUARIO (ID_USUARIO, DS_LOGIN, DS_SENHA_HASH, TP_PERFIL, ID_VETERINARIO, ID_RESPONSAVEL, AT_ATIVO) VALUES
    (2, 'carlos.lima@petbuddies.com.br',  '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4HhSeedTecoRuKa2Vh0LxUWQ9Wsi', 'VET',   2,    NULL, 1);
INSERT INTO T_PB_USUARIO (ID_USUARIO, DS_LOGIN, DS_SENHA_HASH, TP_PERFIL, ID_VETERINARIO, ID_RESPONSAVEL, AT_ATIVO) VALUES
    (3, 'gabriel.nogueira@email.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4HhSeedTecoRuKa2Vh0LxUWQ9Wsi', 'TUTOR', NULL, 1,    1);
INSERT INTO T_PB_USUARIO (ID_USUARIO, DS_LOGIN, DS_SENHA_HASH, TP_PERFIL, ID_VETERINARIO, ID_RESPONSAVEL, AT_ATIVO) VALUES
    (4, 'helena.cardoso@email.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4HhSeedTecoRuKa2Vh0LxUWQ9Wsi', 'TUTOR', NULL, 2,    1);
INSERT INTO T_PB_USUARIO (ID_USUARIO, DS_LOGIN, DS_SENHA_HASH, TP_PERFIL, ID_VETERINARIO, ID_RESPONSAVEL, AT_ATIVO) VALUES
    (5, 'igor.bertolucci@email.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4HhSeedTecoRuKa2Vh0LxUWQ9Wsi', 'TUTOR', NULL, 3,    0);


-- =============================================================================
-- 13. T_PB_PROTOCOLO - o catálogo da clínica
-- =============================================================================
INSERT INTO T_PB_PROTOCOLO (ID_PROTOCOLO, NM_NOME, TP_CATEGORIA_PROTOCOLO, ES_ESPECIE, AT_ATIVO, DS_DESCRICAO) VALUES
    (1, 'Preventivo Cão Adulto',    'PREVENTIVO',    'CACHORRO', 1,
        'Vacinação anual, vermifugação trimestral e controle mensal de ectoparasitas.');
INSERT INTO T_PB_PROTOCOLO (ID_PROTOCOLO, NM_NOME, TP_CATEGORIA_PROTOCOLO, ES_ESPECIE, AT_ATIVO, DS_DESCRICAO) VALUES
    (2, 'Preventivo Gato Adulto',   'PREVENTIVO',    'GATO',     1,
        'Vacinação anual e vermifugação semestral.');
INSERT INTO T_PB_PROTOCOLO (ID_PROTOCOLO, NM_NOME, TP_CATEGORIA_PROTOCOLO, ES_ESPECIE, AT_ATIVO, DS_DESCRICAO) VALUES
    (3, 'Preventivo Filhote Cão',   'PREVENTIVO',    'CACHORRO', 1,
        'Protocolo vacinal de filhote, três doses a partir das seis semanas.');
INSERT INTO T_PB_PROTOCOLO (ID_PROTOCOLO, NM_NOME, TP_CATEGORIA_PROTOCOLO, ES_ESPECIE, AT_ATIVO, DS_DESCRICAO) VALUES
    (4, 'Pós-cirúrgico Ortopédico', 'POS_CIRURGICO', 'CACHORRO', 1,
        'Acompanhamento de dez dias após osteossíntese.');
INSERT INTO T_PB_PROTOCOLO (ID_PROTOCOLO, NM_NOME, TP_CATEGORIA_PROTOCOLO, ES_ESPECIE, AT_ATIVO, DS_DESCRICAO) VALUES
    (5, 'Preventivo Coelho',        'PREVENTIVO',    'COELHO',   1,
        'Vermifugação semestral e checagem dentária.');


-- =============================================================================
-- 14. T_PB_REGRA_PROTOCOLO - o molde do item
-- =============================================================================
INSERT INTO T_PB_REGRA_PROTOCOLO (ID_REGRA_PROTOCOLO, ID_PROTOCOLO, TP_TIPO_CUIDADO, NM_NOME, NR_OFFSET,
                                  TP_UNIDADE_OFFSET, TP_DATA_BASE, NR_INTERVALO, TP_UNIDADE_INTERVALO,
                                  NR_REPETICOES, DS_DESCRICAO) VALUES
    (1, 1, 'VACINACAO',    'Vacina V10 (reforço anual)', 12, 'MESES', 'ULTIMA_REALIZACAO', NULL, NULL, 1,
        'Reavaliada a cada leitura: vence doze meses depois da última aplicação.');
INSERT INTO T_PB_REGRA_PROTOCOLO (ID_REGRA_PROTOCOLO, ID_PROTOCOLO, TP_TIPO_CUIDADO, NM_NOME, NR_OFFSET,
                                  TP_UNIDADE_OFFSET, TP_DATA_BASE, NR_INTERVALO, TP_UNIDADE_INTERVALO,
                                  NR_REPETICOES, DS_DESCRICAO) VALUES
    (2, 1, 'VERMIFUGACAO', 'Vermífugo oral',              3, 'MESES', 'ULTIMA_REALIZACAO', NULL, NULL, 1,
        'O tutor administra em casa.');
INSERT INTO T_PB_REGRA_PROTOCOLO (ID_REGRA_PROTOCOLO, ID_PROTOCOLO, TP_TIPO_CUIDADO, NM_NOME, NR_OFFSET,
                                  TP_UNIDADE_OFFSET, TP_DATA_BASE, NR_INTERVALO, TP_UNIDADE_INTERVALO,
                                  NR_REPETICOES, DS_DESCRICAO) VALUES
    (3, 1, 'HIGIENE',      'Antipulgas tópico',           1, 'MESES', 'ULTIMA_REALIZACAO', NULL, NULL, 1,
        'Mensal.');
INSERT INTO T_PB_REGRA_PROTOCOLO (ID_REGRA_PROTOCOLO, ID_PROTOCOLO, TP_TIPO_CUIDADO, NM_NOME, NR_OFFSET,
                                  TP_UNIDADE_OFFSET, TP_DATA_BASE, NR_INTERVALO, TP_UNIDADE_INTERVALO,
                                  NR_REPETICOES, DS_DESCRICAO) VALUES
    (4, 2, 'VACINACAO',    'Vacina quádrupla felina',    12, 'MESES', 'ULTIMA_REALIZACAO', NULL, NULL, 1,
        'É esta regra que a vacina de 2025 do animal 2 faz vencer.');

INSERT INTO T_PB_REGRA_PROTOCOLO (ID_REGRA_PROTOCOLO, ID_PROTOCOLO, TP_TIPO_CUIDADO, NM_NOME, NR_OFFSET,
                                  TP_UNIDADE_OFFSET, TP_DATA_BASE, NR_INTERVALO, TP_UNIDADE_INTERVALO,
                                  NR_REPETICOES, DS_DESCRICAO) VALUES
    (5, 4, 'RETORNO',      'Retirada de pontos',         10, 'DIAS',  'DATA_CIRURGIA',     NULL, NULL, 1,
        'Retorno único, dez dias após a cirurgia.');

INSERT INTO T_PB_REGRA_PROTOCOLO (ID_REGRA_PROTOCOLO, ID_PROTOCOLO, TP_TIPO_CUIDADO, NM_NOME, NR_OFFSET,
                                  TP_UNIDADE_OFFSET, TP_DATA_BASE, NR_INTERVALO, TP_UNIDADE_INTERVALO,
                                  NR_REPETICOES, DS_DESCRICAO) VALUES
    (6, 3, 'VACINACAO',    'Vacina V10 (filhote)',       45, 'DIAS',  'NASCIMENTO',          21, 'DIAS', 3,
        'Três doses com 21 dias de intervalo.');


-- =============================================================================
-- 15. T_PB_CHECKIN - os relatos GERAIS do dia
-- =============================================================================
INSERT INTO T_PB_CHECKIN (ID_CHECKIN, ID_ANIMAL, ID_ITEM_PLANO_CUIDADO, DT_REFERENCIA,
                          DH_REGISTRADO_EM, TX_NARRATIVA, TX_OBSERVACOES_GERAIS, DS_TIC_UTILIZADA) VALUES
    (1, 1, NULL, DATE '2026-09-01', TIMESTAMP '2026-09-01 08:15:00',
       'O Thor passou bem a noite, comeu tudo e não vomitou. Medi a temperatura e deu 38.5.',
       'Tutor relatou por áudio', 'PetBuddies App - transcricao on-device');
INSERT INTO T_PB_CHECKIN (ID_CHECKIN, ID_ANIMAL, ID_ITEM_PLANO_CUIDADO, DT_REFERENCIA,
                          DH_REGISTRADO_EM, TX_NARRATIVA, TX_OBSERVACOES_GERAIS, DS_TIC_UTILIZADA) VALUES
    (2, 1, NULL, DATE '2026-09-02', TIMESTAMP '2026-09-02 08:10:00',
       'Hoje ele vomitou duas vezes de manhã e teve um episódio de tremor forte que parecia convulsão.',
       'Tutor muito preocupado', 'PetBuddies App - transcricao on-device');
INSERT INTO T_PB_CHECKIN (ID_CHECKIN, ID_ANIMAL, ID_ITEM_PLANO_CUIDADO, DT_REFERENCIA,
                          DH_REGISTRADO_EM, TX_NARRATIVA, TX_OBSERVACOES_GERAIS, DS_TIC_UTILIZADA) VALUES
    (3, 2, NULL, DATE '2026-09-01', TIMESTAMP '2026-09-01 09:00:00',
       'A Mia está bem, sem vômito, comendo normal.', NULL, 'PetBuddies App - texto');
INSERT INTO T_PB_CHECKIN (ID_CHECKIN, ID_ANIMAL, ID_ITEM_PLANO_CUIDADO, DT_REFERENCIA,
                          DH_REGISTRADO_EM, TX_NARRATIVA, TX_OBSERVACOES_GERAIS, DS_TIC_UTILIZADA) VALUES
    (4, 3, NULL, DATE '2026-09-01', TIMESTAMP '2026-09-01 10:30:00',
       'O Bidu teve as fezes bem moles hoje cedo, quase liquidas.', NULL, 'PetBuddies App - texto');
INSERT INTO T_PB_CHECKIN (ID_CHECKIN, ID_ANIMAL, ID_ITEM_PLANO_CUIDADO, DT_REFERENCIA,
                          DH_REGISTRADO_EM, TX_NARRATIVA, TX_OBSERVACOES_GERAIS, DS_TIC_UTILIZADA) VALUES
    (5, 4, NULL, DATE '2026-09-01', TIMESTAMP '2026-09-01 11:45:00',
       'A Nina comeu bem menos que o normal, mas nao vomitou nem teve diarreia.',
       NULL, 'PetBuddies App - transcricao on-device');
INSERT INTO T_PB_CHECKIN (ID_CHECKIN, ID_ANIMAL, ID_ITEM_PLANO_CUIDADO, DT_REFERENCIA,
                          DH_REGISTRADO_EM, TX_NARRATIVA, TX_OBSERVACOES_GERAIS, DS_TIC_UTILIZADA) VALUES
    (6, 2, NULL, DATE '2026-09-02', TIMESTAMP '2026-09-02 09:05:00',
       'Segundo dia: a Mia continua sem vomitar e com apetite normal.', NULL, 'PetBuddies App - texto');


-- =============================================================================
-- 16. T_PB_CONDICAO_OBSERVADA - o que a IA extraiu de cada relato
-- =============================================================================
INSERT INTO T_PB_CONDICAO_OBSERVADA (ID_CONDICAO_OBSERVADA, ID_CHECKIN, ID_CONDICAO_CLINICA,
                                     CD_CODIGO_CONGELADO, BL_VALOR_BOOLEANO, NR_VALOR_NUMERICO, NR_CONFIANCA) VALUES
    (1, 1, 1, 'VOMITO',      0,    NULL,   0.9600);
INSERT INTO T_PB_CONDICAO_OBSERVADA (ID_CONDICAO_OBSERVADA, ID_CHECKIN, ID_CONDICAO_CLINICA,
                                     CD_CODIGO_CONGELADO, BL_VALOR_BOOLEANO, NR_VALOR_NUMERICO, NR_CONFIANCA) VALUES
    (2, 1, 3, 'TEMPERATURA', NULL, 38.500, 0.9900);
INSERT INTO T_PB_CONDICAO_OBSERVADA (ID_CONDICAO_OBSERVADA, ID_CHECKIN, ID_CONDICAO_CLINICA,
                                     CD_CODIGO_CONGELADO, BL_VALOR_BOOLEANO, NR_VALOR_NUMERICO, NR_CONFIANCA) VALUES
    (3, 2, 1, 'VOMITO',      1,    NULL,   0.9800);
INSERT INTO T_PB_CONDICAO_OBSERVADA (ID_CONDICAO_OBSERVADA, ID_CHECKIN, ID_CONDICAO_CLINICA,
                                     CD_CODIGO_CONGELADO, BL_VALOR_BOOLEANO, NR_VALOR_NUMERICO, NR_CONFIANCA) VALUES
    (4, 2, 5, 'CONVULSAO',   1,    NULL,   0.7400);
INSERT INTO T_PB_CONDICAO_OBSERVADA (ID_CONDICAO_OBSERVADA, ID_CHECKIN, ID_CONDICAO_CLINICA,
                                     CD_CODIGO_CONGELADO, BL_VALOR_BOOLEANO, NR_VALOR_NUMERICO, NR_CONFIANCA) VALUES
    (5, 3, 1, 'VOMITO',      0,    NULL,   0.9500);
INSERT INTO T_PB_CONDICAO_OBSERVADA (ID_CONDICAO_OBSERVADA, ID_CHECKIN, ID_CONDICAO_CLINICA,
                                     CD_CODIGO_CONGELADO, BL_VALOR_BOOLEANO, NR_VALOR_NUMERICO, NR_CONFIANCA) VALUES
    (6, 4, 2, 'DIARREIA',    1,    NULL,   0.9100);
-- Condição observada que NENHUMA regra cobre: fica registrada mesmo assim, para
-- a veterinária ver. É por isso que a observação aponta para o catálogo, e não
-- para a regra.
INSERT INTO T_PB_CONDICAO_OBSERVADA (ID_CONDICAO_OBSERVADA, ID_CHECKIN, ID_CONDICAO_CLINICA,
                                     CD_CODIGO_CONGELADO, BL_VALOR_BOOLEANO, NR_VALOR_NUMERICO, NR_CONFIANCA) VALUES
    (7, 5, 4, 'APETITE_BAIXO', 1,  NULL,   0.8800);
INSERT INTO T_PB_CONDICAO_OBSERVADA (ID_CONDICAO_OBSERVADA, ID_CHECKIN, ID_CONDICAO_CLINICA,
                                     CD_CODIGO_CONGELADO, BL_VALOR_BOOLEANO, NR_VALOR_NUMERICO, NR_CONFIANCA) VALUES
    (8, 6, 1, 'VOMITO',      0,    NULL,   0.9700);

-- =============================================================================
-- 17. T_PB_PLANO_CUIDADO - as três origens de plano
-- =============================================================================
INSERT INTO T_PB_PLANO_CUIDADO (ID_PLANO_CUIDADO, ID_ANIMAL, ID_CONSULTA, ID_PROTOCOLO, TP_CATEGORIA_PLANO, ST_STATUS_PLANO) VALUES
    (1, 1, NULL, 1,    'PREVENTIVO',    'ATIVO');
INSERT INTO T_PB_PLANO_CUIDADO (ID_PLANO_CUIDADO, ID_ANIMAL, ID_CONSULTA, ID_PROTOCOLO, TP_CATEGORIA_PLANO, ST_STATUS_PLANO) VALUES
    (2, 1, 1,    NULL, 'TRATAMENTO',    'ATIVO');
INSERT INTO T_PB_PLANO_CUIDADO (ID_PLANO_CUIDADO, ID_ANIMAL, ID_CONSULTA, ID_PROTOCOLO, TP_CATEGORIA_PLANO, ST_STATUS_PLANO) VALUES
    (3, 2, NULL, 2,    'PREVENTIVO',    'ATIVO');
INSERT INTO T_PB_PLANO_CUIDADO (ID_PLANO_CUIDADO, ID_ANIMAL, ID_CONSULTA, ID_PROTOCOLO, TP_CATEGORIA_PLANO, ST_STATUS_PLANO) VALUES
    (4, 2, 2,    NULL, 'TRATAMENTO',    'ATIVO');
INSERT INTO T_PB_PLANO_CUIDADO (ID_PLANO_CUIDADO, ID_ANIMAL, ID_CONSULTA, ID_PROTOCOLO, TP_CATEGORIA_PLANO, ST_STATUS_PLANO) VALUES
    (5, 3, 3,    4,    'POS_CIRURGICO', 'ATIVO');
INSERT INTO T_PB_PLANO_CUIDADO (ID_PLANO_CUIDADO, ID_ANIMAL, ID_CONSULTA, ID_PROTOCOLO, TP_CATEGORIA_PLANO, ST_STATUS_PLANO) VALUES
    (6, 3, 3,    NULL, 'TRATAMENTO',    'ATIVO');
INSERT INTO T_PB_PLANO_CUIDADO (ID_PLANO_CUIDADO, ID_ANIMAL, ID_CONSULTA, ID_PROTOCOLO, TP_CATEGORIA_PLANO, ST_STATUS_PLANO) VALUES
    (7, 4, 4,    NULL, 'TRATAMENTO',    'ATIVO');
INSERT INTO T_PB_PLANO_CUIDADO (ID_PLANO_CUIDADO, ID_ANIMAL, ID_CONSULTA, ID_PROTOCOLO, TP_CATEGORIA_PLANO, ST_STATUS_PLANO) VALUES
    (8, 5, NULL, 3,    'PREVENTIVO',    'ATIVO');


-- =============================================================================
-- 18. T_PB_ITEM_PLANO_CUIDADO - o item, agora com a execução dentro
-- =============================================================================
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (1, 2, NULL, 'PRESCRICAO', 1, 'MEDICACAO', 'Amoxicilina - dia 1', DATE '2026-09-01', 'REALIZADO',
        NULL, NULL, TIMESTAMP '2026-09-01 08:20:00', 1, 'DOSE_CALCULADA', 0.500, NULL);
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (2, 2, NULL, 'PRESCRICAO', 3, 'MEDICACAO', 'Dipirona - dia 1',    DATE '2026-09-01', 'REALIZADO',
        NULL, NULL, TIMESTAMP '2026-09-01 08:25:00', 1, 'DOSE_CALCULADA', 1.000, NULL);
-- Suspenso pela regra: o pet vomitou. ST_STATUS_ITEM diz que não foi feito;
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (3, 2, NULL, 'PRESCRICAO', 1, 'MEDICACAO', 'Amoxicilina - dia 2', DATE '2026-09-02', 'CANCELADO',
        'Suspensa pela regra: o pet vomitou', NULL, NULL, 2, 'SEM_DOSE', NULL, 1);
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (4, 2, NULL, 'PRESCRICAO', 3, 'MEDICACAO', 'Dipirona - dia 2',    DATE '2026-09-02', 'CANCELADO',
        'Convulsão relatada: clínica acionada', NULL, NULL, 2, 'ACIONAR_CLINICA', NULL, 3);
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (5, 4, NULL, 'PRESCRICAO', 2, 'MEDICACAO', 'Amoxicilina - dia 1', DATE '2026-09-01', 'REALIZADO',
        NULL, NULL, TIMESTAMP '2026-09-01 09:10:00', 3, 'DOSE_CALCULADA', 0.750, NULL);
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (6, 6, NULL, 'PRESCRICAO', 4, 'MEDICACAO', 'Meloxicam - dia 1',   DATE '2026-09-01', 'REALIZADO',
        'Dose mínima: diarreia relatada', NULL, TIMESTAMP '2026-09-01 10:40:00', 4, 'DOSE_CALCULADA', 0.250, 6);
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (7, 7, NULL, 'PRESCRICAO', 5, 'MEDICACAO', 'Prednisolona - dia 1',DATE '2026-09-01', 'REALIZADO',
        NULL, NULL, TIMESTAMP '2026-09-01 12:00:00', 5, 'DOSE_CALCULADA', 2.000, NULL);
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (8, 4, NULL, 'PRESCRICAO', 2, 'MEDICACAO', 'Amoxicilina - dia 2', DATE '2026-09-02', 'REALIZADO',
        NULL, NULL, TIMESTAMP '2026-09-02 09:15:00', 6, 'DOSE_CALCULADA', 0.750, NULL);

-- ITENS DE PROTOCOLO: as quatro colunas de execução por check-in ficam NULAS 
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (9, 1, 1, 'PROTOCOLO', NULL, 'VACINACAO',    'Vacina V10 (reforço anual)', DATE '2026-09-15', 'PENDENTE',
        NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (10, 1, 2, 'PROTOCOLO', NULL, 'VERMIFUGACAO','Vermífugo oral',             DATE '2026-09-11', 'PENDENTE',
        NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (11, 1, 3, 'PROTOCOLO', NULL, 'HIGIENE',     'Antipulgas tópico',          DATE '2026-09-27', 'PENDENTE',
        NULL, NULL, NULL, NULL, NULL, NULL, NULL);
-- ESTE É O ITEM QUE FAZ A PERIODICIDADE EXISTIR. 
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (12, 3, 4, 'PROTOCOLO', NULL, 'VACINACAO',   'Vacina quádrupla felina',    DATE '2025-08-20', 'REALIZADO',
        NULL, 6, TIMESTAMP '2025-08-20 10:30:00', NULL, NULL, NULL, NULL);
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (13, 5, 5, 'PROTOCOLO', NULL, 'RETORNO',     'Retirada de pontos',         DATE '2026-09-08', 'PENDENTE',
        NULL, NULL, NULL, NULL, NULL, NULL, NULL);
-- Item vencido e não feito: o que a tela do tutor mostra em vermelho.
INSERT INTO T_PB_ITEM_PLANO_CUIDADO (ID_ITEM_PLANO_CUIDADO, ID_PLANO_CUIDADO, ID_REGRA_PROTOCOLO, TP_ORIGEM,
                                     ID_PRESCRICAO, TP_TIPO_CUIDADO, NM_NOME, DT_DATA_ALVO, ST_STATUS_ITEM, OB_OBSERVACAO,
                                     ID_PROCEDIMENTO, DT_EXECUTADO_EM,
                                     ID_CHECKIN, TP_DESFECHO, NR_DOSE_APLICADA, ID_REGRA_APLICADA) VALUES
    (14, 8, 6, 'PROTOCOLO', NULL, 'VACINACAO',   'Vacina V10 (filhote) - 1a dose', DATE '2026-08-20', 'ATRASADO',
        'Tutor não compareceu', NULL, NULL, NULL, NULL, NULL, NULL);

-- =============================================================================
-- 19. T_PB_CHECKIN aponta para o item de que fala; o item aponta para o check-in que
-- o baixou. 
-- =============================================================================
INSERT INTO T_PB_CHECKIN (ID_CHECKIN, ID_ANIMAL, ID_ITEM_PLANO_CUIDADO, DT_REFERENCIA,
                          DH_REGISTRADO_EM, TX_NARRATIVA, TX_OBSERVACOES_GERAIS, DS_TIC_UTILIZADA) VALUES
    (7, 1, 1, DATE '2026-09-01', TIMESTAMP '2026-09-01 20:30:00',
       'Sobre a amoxicilina de hoje: ele tomou direitinho junto com a ração, sem cuspir nada.',
       'Relato específico sobre um item do checklist', 'PetBuddies App - texto');


-- =============================================================================
-- 20. T_PB_OFERTA - o preço, e desde quando
-- =============================================================================
INSERT INTO T_PB_OFERTA (ID_OFERTA, ID_CLINICA, TP_ATO, TP_SUBTIPO, ID_PROTOCOLO,
                         DS_DESCRICAO, NR_VALOR, DT_INICIO_VIGENCIA) VALUES
    (1, 1, 'CONSULTA', 'ROTINA', NULL, 'Consulta de rotina', 180.00, DATE '2026-01-01');
INSERT INTO T_PB_OFERTA (ID_OFERTA, ID_CLINICA, TP_ATO, TP_SUBTIPO, ID_PROTOCOLO,
                         DS_DESCRICAO, NR_VALOR, DT_INICIO_VIGENCIA) VALUES
    (2, 1, 'CONSULTA', 'ROTINA', NULL, 'Consulta de rotina', 200.00, DATE '2026-07-01');
INSERT INTO T_PB_OFERTA (ID_OFERTA, ID_CLINICA, TP_ATO, TP_SUBTIPO, ID_PROTOCOLO,
                         DS_DESCRICAO, NR_VALOR, DT_INICIO_VIGENCIA) VALUES
    (3, 1, 'PROCEDIMENTO', 'VACINACAO', NULL, 'Vacina V10 (dose)', 120.00, DATE '2026-01-01');
INSERT INTO T_PB_OFERTA (ID_OFERTA, ID_CLINICA, TP_ATO, TP_SUBTIPO, ID_PROTOCOLO,
                         DS_DESCRICAO, NR_VALOR, DT_INICIO_VIGENCIA) VALUES
    (4, 1, 'PROCEDIMENTO', 'EXAME_LABORATORIAL', NULL, 'Hemograma completo', 95.00, DATE '2026-01-01');
-- Protocolo vendido como pacote: aqui o alvo é uma LINHA, não um valor de enum.
INSERT INTO T_PB_OFERTA (ID_OFERTA, ID_CLINICA, TP_ATO, TP_SUBTIPO, ID_PROTOCOLO,
                         DS_DESCRICAO, NR_VALOR, DT_INICIO_VIGENCIA) VALUES
    (5, 1, 'PROTOCOLO', NULL, 1, 'Pacote preventivo cão adulto (anual)', 450.00, DATE '2026-01-01');
INSERT INTO T_PB_OFERTA (ID_OFERTA, ID_CLINICA, TP_ATO, TP_SUBTIPO, ID_PROTOCOLO,
                         DS_DESCRICAO, NR_VALOR, DT_INICIO_VIGENCIA) VALUES
    (6, 1, 'PROTOCOLO', NULL, 3, 'Pacote filhote (três doses)', 380.00, DATE '2026-01-01');


-- =============================================================================
-- 21. T_PB_REGRA_PONTUACAO - quanto vale cada gesto
-- =============================================================================
INSERT INTO T_PB_REGRA_PONTUACAO (ID_REGRA_PONTUACAO, ID_CLINICA, TP_GESTO, NR_PONTOS, DT_INICIO_VIGENCIA) VALUES
    (1, 1, 'PLANO_CRIADO',           50, DATE '2026-01-01');
INSERT INTO T_PB_REGRA_PONTUACAO (ID_REGRA_PONTUACAO, ID_CLINICA, TP_GESTO, NR_PONTOS, DT_INICIO_VIGENCIA) VALUES
    (2, 1, 'CONSULTA_AGENDADA',      10, DATE '2026-01-01');
INSERT INTO T_PB_REGRA_PONTUACAO (ID_REGRA_PONTUACAO, ID_CLINICA, TP_GESTO, NR_PONTOS, DT_INICIO_VIGENCIA) VALUES
    (3, 1, 'CONSULTA_REALIZADA',     30, DATE '2026-01-01');
INSERT INTO T_PB_REGRA_PONTUACAO (ID_REGRA_PONTUACAO, ID_CLINICA, TP_GESTO, NR_PONTOS, DT_INICIO_VIGENCIA) VALUES
    (4, 1, 'PROCEDIMENTO_EXECUTADO', 20, DATE '2026-01-01');
-- Comparecer passou a valer mais a partir de julho: é o gesto que a clínica quer
-- estimular, e mudar isso é configuração, não deploy.
INSERT INTO T_PB_REGRA_PONTUACAO (ID_REGRA_PONTUACAO, ID_CLINICA, TP_GESTO, NR_PONTOS, DT_INICIO_VIGENCIA) VALUES
    (5, 1, 'CONSULTA_REALIZADA',     40, DATE '2026-07-01');
INSERT INTO T_PB_REGRA_PONTUACAO (ID_REGRA_PONTUACAO, ID_CLINICA, TP_GESTO, NR_PONTOS, DT_INICIO_VIGENCIA) VALUES
    (6, 1, 'PLANO_CRIADO',           60, DATE '2026-07-01');

COMMIT;

-- =============================================================================
-- REALINHAMENTO DOS GERADORES 
-- =============================================================================
DECLARE
    TYPE t_tabelas IS TABLE OF VARCHAR2(40);
    v_tabelas t_tabelas := t_tabelas(
        'T_PB_CLINICA',            'T_PB_VETERINARIO',       'T_PB_RESPONSAVEL',
        'T_PB_ANIMAL',             'T_PB_CONDICAO_CLINICA',  'T_PB_CONSULTA',
        'T_PB_JANELA_ATENDIMENTO', 'T_PB_REGISTRO_ATENDIMENTO', 'T_PB_PROCEDIMENTO',
        'T_PB_PRESCRICAO',         'T_PB_REGRA_PRESCRICAO',  'T_PB_CHECKIN',
        'T_PB_CONDICAO_OBSERVADA', 'T_PB_USUARIO',           'T_PB_PROTOCOLO',
        'T_PB_REGRA_PROTOCOLO',    'T_PB_PLANO_CUIDADO',     'T_PB_ITEM_PLANO_CUIDADO',
        'T_PB_OFERTA',             'T_PB_REGRA_PONTUACAO',   'T_PB_AUDITORIA');
    v_coluna  VARCHAR2(40);
BEGIN
    FOR i IN 1 .. v_tabelas.COUNT LOOP
     
        SELECT column_name INTO v_coluna
          FROM user_tab_identity_cols
         WHERE table_name = v_tabelas(i);

        EXECUTE IMMEDIATE 'ALTER TABLE ' || v_tabelas(i) ||
                          ' MODIFY ' || v_coluna ||
                          ' GENERATED BY DEFAULT ON NULL AS IDENTITY (START WITH LIMIT VALUE)';
    END LOOP;
    DBMS_OUTPUT.PUT_LINE('Geradores realinhados nas ' || v_tabelas.COUNT || ' tabelas.');
END;
/
