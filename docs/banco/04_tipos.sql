-- =============================================================================
-- 04 — TIPOS DE APOIO DOS PACOTES
-- =============================================================================
-- Tipos de objeto ficam fora dos pacotes de propósito: um tipo declarado dentro
-- de um pacote só existe em PL/SQL, e estes três atravessam a fronteira SQL —
-- PB_PARES_JSON é construído em chamadas de SQL e PB_RESULTADO_DOSE é o retorno
-- da função de dose. Por isso são de schema, e os pacotes do bloco 05 os usam.
--
-- FORCE substitui o tipo mesmo com dependentes (o pacote que já o usa, ou a
-- tabela de pares que depende do par), sem o DROP antes — rodar o bloco duas
-- vezes não falha.
-- =============================================================================

-- Um par (campo, valor, tipo) que PKG_PB_JSON.MONTA_JSON transforma em JSON.
-- TP_TIPO é o tipo do VALOR no JSON: TEXTO, NUMERO, BOOLEANO, DATA, NULO ou JSON.
CREATE OR REPLACE TYPE PB_PAR_JSON FORCE AS OBJECT (
    NM_CAMPO  VARCHAR2(128),
    VL_VALOR  CLOB,
    TP_TIPO   VARCHAR2(20)
);
/

CREATE OR REPLACE TYPE PB_PARES_JSON FORCE AS TABLE OF PB_PAR_JSON;
/

-- O que PKG_PB_TRATAMENTO.DOSE_DO_DIA devolve.
--
-- Espelha exatamente as três colunas de decisão que a fusão do ADR s3-24 levou
-- para dentro de T_PB_ITEM_PLANO_CUIDADO: TP_DESFECHO, NR_DOSE_APLICADA e
-- ID_REGRA_APLICADA. Devolver só o número perderia os dois desfechos que não têm
-- dose (SEM_DOSE e ACIONAR_CLINICA), e o chamador teria de recalcular o motivo —
-- que é justamente o que a função existe para não deixar acontecer duas vezes.
CREATE OR REPLACE TYPE PB_RESULTADO_DOSE FORCE AS OBJECT (
    TP_DESFECHO        VARCHAR2(30),
    NR_DOSE_APLICADA   NUMBER(8,3),
    ID_REGRA_APLICADA  NUMBER(10)
);
/
