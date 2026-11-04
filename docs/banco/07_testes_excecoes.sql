SET SERVEROUTPUT ON SIZE UNLIMITED

PROMPT
PROMPT ############################################################
PROMPT # 1. FUNÇÃO 1 - PB_MONTA_JSON
PROMPT ############################################################

-- ---------- 1.1 caminho feliz -----------------------------------------------
DECLARE
    v_json CLOB;
BEGIN
    v_json := PB_MONTA_JSON(PB_PARES_JSON(
        PB_PAR_JSON('nome',      'Thor',      'TEXTO'),
        PB_PAR_JSON('especie',   'CACHORRO',  'TEXTO'),
        PB_PAR_JSON('pesoKg',    '32.5',      'NUMERO'),
        PB_PAR_JSON('castrado',  '1',         'BOOLEANO'),
        PB_PAR_JSON('nascimento','2021-03-14','DATA'),
        PB_PAR_JSON('obito',     NULL,        'NULO')
    ));
    DBMS_OUTPUT.PUT_LINE('1.1 OK  -> ' || v_json);
END;
/

-- ---------- 1.2 o escape funciona -------------------------------------------
-- Aspas, barra invertida e quebra de linha dentro do texto. Sem escape, isto
-- geraria JSON que nenhum parser aceita.
DECLARE
    v_json CLOB;
BEGIN
    v_json := PB_MONTA_JSON(PB_PARES_JSON(
        PB_PAR_JSON('observacao', 'Tutor disse: "vomitou 2x"' || CHR(10) || 'caminho C:\dados',
                    'TEXTO')
    ));
    DBMS_OUTPUT.PUT_LINE('1.2 OK  -> ' || v_json);
END;
/

-- ---------- 1.3 EXCEÇÃO: coleção vazia (-20001) -----------------------------
DECLARE
    v_json CLOB;
BEGIN
    v_json := PB_MONTA_JSON(PB_PARES_JSON());
    DBMS_OUTPUT.PUT_LINE('1.3 FALHOU: deveria ter levantado exceção');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('1.3 EXCECAO TRATADA (' || SQLCODE || ') -> ' || SQLERRM);
END;
/

-- ---------- 1.4 EXCEÇÃO: tipo desconhecido (-20003) -------------------------
DECLARE
    v_json CLOB;
BEGIN
    v_json := PB_MONTA_JSON(PB_PARES_JSON(
        PB_PAR_JSON('peso', '32.5', 'DECIMAL')   -- tipo que não existe
    ));
    DBMS_OUTPUT.PUT_LINE('1.4 FALHOU: deveria ter levantado exceção');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('1.4 EXCECAO TRATADA (' || SQLCODE || ') -> ' || SQLERRM);
END;
/

-- ---------- 1.5 EXCEÇÃO: NUMERO com valor não numérico (-20004) -------------
DECLARE
    v_json CLOB;
BEGIN
    v_json := PB_MONTA_JSON(PB_PARES_JSON(
        PB_PAR_JSON('peso', 'trinta e dois', 'NUMERO')
    ));
    DBMS_OUTPUT.PUT_LINE('1.5 FALHOU: deveria ter levantado exceção');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('1.5 EXCECAO TRATADA (' || SQLCODE || ') -> ' || SQLERRM);
END;
/


PROMPT
PROMPT ############################################################
PROMPT # 2. FUNÇÃO 2 - PB_DOSE_DO_DIA
PROMPT ############################################################

-- ---------- 2.1 o motor confere com a carga ---------------------------------
-- Este é o teste mais forte do bloco: para cada item de TRATAMENTO semeado em
-- T_PB_ITEM_PLANO_CUIDADO, recalcula a dose com a função e compara com o que
-- está gravado. Se a carga e o motor discordarem, aparece aqui — não na banca.

DECLARE
    v_calc  PB_RESULTADO_DOSE;
    v_ok    PLS_INTEGER := 0;
    v_erro  PLS_INTEGER := 0;
BEGIN
    FOR r IN (SELECT ID_ITEM_PLANO_CUIDADO, ID_CHECKIN, ID_PRESCRICAO,
                     TP_DESFECHO, NR_DOSE_APLICADA
                FROM T_PB_ITEM_PLANO_CUIDADO
               WHERE TP_ORIGEM = 'PRESCRICAO'
                 AND ID_CHECKIN IS NOT NULL
               ORDER BY ID_ITEM_PLANO_CUIDADO)
    LOOP
        v_calc := PB_DOSE_DO_DIA(r.ID_PRESCRICAO, r.ID_CHECKIN);

        IF v_calc.TP_DESFECHO = r.TP_DESFECHO
           AND NVL(v_calc.NR_DOSE_APLICADA, -1) = NVL(r.NR_DOSE_APLICADA, -1) THEN
            v_ok := v_ok + 1;
            DBMS_OUTPUT.PUT_LINE('2.1 item ' || r.ID_ITEM_PLANO_CUIDADO
                || ': OK  -> ' || v_calc.TP_DESFECHO
                || ' dose=' || NVL(TO_CHAR(v_calc.NR_DOSE_APLICADA), 'null')
                || ' regra=' || NVL(TO_CHAR(v_calc.ID_REGRA_APLICADA), 'nenhuma'));
        ELSE
            v_erro := v_erro + 1;
            DBMS_OUTPUT.PUT_LINE('2.1 item ' || r.ID_ITEM_PLANO_CUIDADO
                || ': DIVERGE -> gravado ' || r.TP_DESFECHO || '/'
                || NVL(TO_CHAR(r.NR_DOSE_APLICADA), 'null')
                || ' , calculado ' || v_calc.TP_DESFECHO || '/'
                || NVL(TO_CHAR(v_calc.NR_DOSE_APLICADA), 'null'));
        END IF;
    END LOOP;
    DBMS_OUTPUT.PUT_LINE('2.1 RESUMO: ' || v_ok || ' conferem, ' || v_erro || ' divergem.');
END;
/

-- ---------- 2.2 EXCEÇÃO: prescrição inexistente (-20011) --------------------
DECLARE
    v_calc PB_RESULTADO_DOSE;
BEGIN
    v_calc := PB_DOSE_DO_DIA(9999, 1);
    DBMS_OUTPUT.PUT_LINE('2.2 FALHOU: deveria ter levantado exceção');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('2.2 EXCECAO TRATADA (' || SQLCODE || ') -> ' || SQLERRM);
END;
/

-- ---------- 2.3 EXCEÇÃO: check-in inexistente (-20012) ----------------------
-- A mais importante das duas: sem ela, a função devolveria a dose padrão como
-- se o tutor tivesse relatado "tudo bem" — medicando com base em relato que
-- nunca existiu.
DECLARE
    v_calc PB_RESULTADO_DOSE;
BEGIN
    v_calc := PB_DOSE_DO_DIA(1, 9999);
    DBMS_OUTPUT.PUT_LINE('2.3 FALHOU: deveria ter levantado exceção');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('2.3 EXCECAO TRATADA (' || SQLCODE || ') -> ' || SQLERRM);
END;
/


PROMPT
PROMPT ############################################################
PROMPT # 3. PROCEDIMENTO 1 - PB_PLANO_JSON  (JOIN de 3 tabelas -> JSON)
PROMPT ############################################################

-- ---------- 3.1 caminho feliz: o plano do Thor, com 6 eventos embutidos -----
BEGIN
    PB_PLANO_JSON(1);
END;
/

-- ---------- 3.2 EXCEÇÃO: animal inexistente (-20021) ------------------------
BEGIN
    PB_PLANO_JSON(9999);
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('3.2 EXCECAO TRATADA (' || SQLCODE || ') -> ' || SQLERRM);
END;
/

-- ---------- 3.3 EXCEÇÃO: animal existe mas não tem plano (-20022) -----------
-- Precisa de um animal sem plano. Como todos os cinco da carga têm, cria-se um
-- descartável e o remove no fim — sem id explícito, o que também prova que o
-- realinhamento dos geradores no bloco 03 funcionou.
DECLARE
    v_id NUMBER;
BEGIN
    INSERT INTO T_PB_ANIMAL (NM_NOME_ANIMAL, ES_ESPECIE, RC_RACA, PT_PORTE, SX_SEXO,
                             DT_DATA_NASCIMENTO, ID_RESPONSAVEL)
    VALUES ('Pet sem plano', 'GATO', 'Sem raça definida', 'PEQUENO', 'FEMEA',
            DATE '2024-01-01', 1)
    RETURNING ID_ANIMAL INTO v_id;

    DBMS_OUTPUT.PUT_LINE('3.3 animal criado com id ' || v_id
                         || ' (gerador realinhado: id > 5)');
    BEGIN
        PB_PLANO_JSON(v_id);
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('3.3 EXCECAO TRATADA (' || SQLCODE || ') -> ' || SQLERRM);
    END;

    DELETE FROM T_PB_ANIMAL WHERE ID_ANIMAL = v_id;
    COMMIT;
END;
/


PROMPT
PROMPT ############################################################
PROMPT # 4. PROCEDIMENTO 2 - PB_ADESAO_POR_DESFECHO  (subtotais manuais)
PROMPT ############################################################

-- ---------- 4.1 caminho feliz: 6 linhas de detalhe, 4 subtotais, total ------

BEGIN
    PB_ADESAO_POR_DESFECHO;
END;
/

-- ---------- 4.2 filtro de período, e a validação do mínimo disparando -------
-- Um dia só produz QUATRO combinações de detalhe, e a disciplina exige cinco.
-- A listagem sai inteira e a exceção -20033 vem depois dela — que é exatamente
-- o comportamento desenhado: o print mostra a saída e o motivo de ela não
-- servir, na mesma tela.
BEGIN
    PB_ADESAO_POR_DESFECHO(DATE '2026-09-01', DATE '2026-09-01');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('4.2 EXCECAO TRATADA (' || SQLCODE || ') -> ' || SQLERRM);
END;
/

-- ---------- 4.3 EXCEÇÃO: período invertido (-20031) -------------------------
BEGIN
    PB_ADESAO_POR_DESFECHO(DATE '2026-09-30', DATE '2026-09-01');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('4.3 EXCECAO TRATADA (' || SQLCODE || ') -> ' || SQLERRM);
END;
/

-- ---------- 4.4 EXCEÇÃO: período sem nenhum resultado (-20032) --------------
BEGIN
    PB_ADESAO_POR_DESFECHO(DATE '2020-01-01', DATE '2020-12-31');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('4.4 EXCECAO TRATADA (' || SQLCODE || ') -> ' || SQLERRM);
END;
/


PROMPT
PROMPT ############################################################
PROMPT # 5. TRIGGER - PB_AUDITA_CONDICAO
PROMPT ############################################################

-- ---------- 5.1 INSERT ------------------------------------------------------
DECLARE
    v_id NUMBER;
BEGIN
    INSERT INTO T_PB_CONDICAO_CLINICA (CD_CODIGO, DS_ROTULO, TP_DADO, TP_FONTE_VALOR, DS_UNIDADE,
                                       FL_CRITICA, AT_ATIVO, ID_CLINICA, ID_VETERINARIO_AUTOR)
    VALUES ('LETARGIA', 'O pet está mais quieto que o normal?', 'BOOLEANO', 'RELATO', NULL,
            0, 1, 1, 1)
    RETURNING ID_CONDICAO_CLINICA INTO v_id;
    DBMS_OUTPUT.PUT_LINE('5.1 INSERT auditado, condição id ' || v_id);
    COMMIT;
END;
/

-- ---------- 5.2 UPDATE: o caso que a trigger existe para pegar --------------
-- Marcar a LETARGIA como crítica e, logo depois, desmarcar. O segundo UPDATE é
-- o cenário de risco: a partir dele a condição deixa de escalar, e nada no
-- sistema avisa. A auditoria é o único lugar onde isso fica registrado.
BEGIN
    UPDATE T_PB_CONDICAO_CLINICA
       SET FL_CRITICA = 1, AT_UPDATED_AT = SYSTIMESTAMP
     WHERE CD_CODIGO = 'LETARGIA' AND ID_CLINICA = 1;
    DBMS_OUTPUT.PUT_LINE('5.2a UPDATE auditado: LETARGIA passou a ser crítica');

    UPDATE T_PB_CONDICAO_CLINICA
       SET FL_CRITICA = 0, AT_UPDATED_AT = SYSTIMESTAMP
     WHERE CD_CODIGO = 'LETARGIA' AND ID_CLINICA = 1;
    DBMS_OUTPUT.PUT_LINE('5.2b UPDATE auditado: LETARGIA DEIXOU de ser crítica'
                         || ' <- é este que a auditoria existe para pegar');
    COMMIT;
END;
/

-- ---------- 5.3 DELETE ------------------------------------------------------
BEGIN
    DELETE FROM T_PB_CONDICAO_CLINICA
     WHERE CD_CODIGO = 'LETARGIA' AND ID_CLINICA = 1;
    DBMS_OUTPUT.PUT_LINE('5.3 DELETE auditado');
    COMMIT;
END;
/

-- ---------- 5.4 o que a auditoria registrou ---------------------------------
PROMPT
PROMPT --- conteudo de T_PB_AUDITORIA ---
SET LINESIZE 200
COLUMN NM_USUARIO   FORMAT A16
COLUMN TP_OPERACAO  FORMAT A8
COLUMN NM_TABELA    FORMAT A22
SELECT ID_AUDITORIA, NM_USUARIO, TP_OPERACAO,
       TO_CHAR(DH_OPERACAO, 'DD/MM/YYYY HH24:MI:SS') AS QUANDO,
       NM_TABELA, ID_REGISTRO
  FROM T_PB_AUDITORIA
 ORDER BY ID_AUDITORIA;

-- Os JSON de :OLD e :NEW, um por linha, montados pela Função 1.
DECLARE
    v_n PLS_INTEGER := 0;
BEGIN
    FOR a IN (SELECT ID_AUDITORIA, TP_OPERACAO, TX_VALORES_ANTERIORES, TX_VALORES_NOVOS
                FROM T_PB_AUDITORIA ORDER BY ID_AUDITORIA)
    LOOP
        v_n := v_n + 1;
        DBMS_OUTPUT.PUT_LINE('--- auditoria ' || a.ID_AUDITORIA || ' (' || a.TP_OPERACAO || ')');
        DBMS_OUTPUT.PUT_LINE('    OLD: ' || NVL(DBMS_LOB.SUBSTR(a.TX_VALORES_ANTERIORES, 3000, 1), '(nao se aplica)'));
        DBMS_OUTPUT.PUT_LINE('    NEW: ' || NVL(DBMS_LOB.SUBSTR(a.TX_VALORES_NOVOS, 3000, 1), '(nao se aplica)'));
    END LOOP;
    DBMS_OUTPUT.PUT_LINE('Total de linhas auditadas: ' || v_n);
END;
/


PROMPT
PROMPT ############################################################
PROMPT # 6. CONFERENCIA FINAL - 5 registros minimos por tabela
PROMPT ############################################################
-- Este bloco conta as vinte e aponta qualquer uma abaixo do mínimo, em vez de deixar a
-- verificação para o olho de quem revisa.
DECLARE
    TYPE t_tabelas IS TABLE OF VARCHAR2(40);
    v_tabelas t_tabelas := t_tabelas(
        'T_PB_CLINICA',            'T_PB_VETERINARIO',       'T_PB_RESPONSAVEL',
        'T_PB_ANIMAL',             'T_PB_CONDICAO_CLINICA',  'T_PB_CONSULTA',
        'T_PB_JANELA_ATENDIMENTO', 'T_PB_REGISTRO_ATENDIMENTO', 'T_PB_PROCEDIMENTO',
        'T_PB_PRESCRICAO',         'T_PB_REGRA_PRESCRICAO',  'T_PB_CHECKIN',
        'T_PB_CONDICAO_OBSERVADA', 'T_PB_USUARIO',
        'T_PB_PROTOCOLO',          'T_PB_REGRA_PROTOCOLO',  'T_PB_PLANO_CUIDADO',
        'T_PB_ITEM_PLANO_CUIDADO', 'T_PB_OFERTA',           'T_PB_REGRA_PONTUACAO',
        'T_PB_AUDITORIA');
    v_qtd    NUMBER;
    v_falhas PLS_INTEGER := 0;
BEGIN
    FOR i IN 1 .. v_tabelas.COUNT LOOP
        EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM ' || v_tabelas(i) INTO v_qtd;
        IF v_qtd < 5 THEN
            v_falhas := v_falhas + 1;
            DBMS_OUTPUT.PUT_LINE('  FALTA  ' || RPAD(v_tabelas(i), 30) || v_qtd || ' registro(s)');
        ELSE
            DBMS_OUTPUT.PUT_LINE('  ok     ' || RPAD(v_tabelas(i), 30) || v_qtd || ' registro(s)');
        END IF;
    END LOOP;

    IF v_falhas = 0 THEN
        DBMS_OUTPUT.PUT_LINE('TODAS as ' || v_tabelas.COUNT || ' tabelas atendem o minimo de 5.');
    ELSE
        DBMS_OUTPUT.PUT_LINE(v_falhas || ' tabela(s) abaixo do minimo exigido.');
    END IF;
END;
/