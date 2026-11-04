DROP TYPE PB_PARES_JSON FORCE;
DROP TYPE PB_PAR_JSON FORCE;

CREATE OR REPLACE TYPE PB_PAR_JSON AS OBJECT (
    NM_CAMPO  VARCHAR2(128),
    VL_VALOR  CLOB,
    TP_TIPO_CUIDADO   VARCHAR2(20)
);
/

CREATE OR REPLACE TYPE PB_PARES_JSON AS TABLE OF PB_PAR_JSON;
/

-- =============================================================================
-- FUNÇÃO 1 - PB_MONTA_JSON
-- =============================================================================
-- Recebe pares relacionais e devolve um objeto JSON como string.
CREATE OR REPLACE FUNCTION PB_MONTA_JSON (
    p_pares PB_PARES_JSON
) RETURN CLOB IS

    -- Erros de negócio da função.
    EX_COLECAO_VAZIA    EXCEPTION;
    EX_CAMPO_SEM_NOME   EXCEPTION;
    EX_TIPO_INVALIDO    EXCEPTION;
    EX_NUMERO_INVALIDO  EXCEPTION;
    EX_TEXTO_LONGO      EXCEPTION;
    PRAGMA EXCEPTION_INIT(EX_COLECAO_VAZIA,   -20001);
    PRAGMA EXCEPTION_INIT(EX_CAMPO_SEM_NOME,  -20002);
    PRAGMA EXCEPTION_INIT(EX_TIPO_INVALIDO,   -20003);
    PRAGMA EXCEPTION_INIT(EX_NUMERO_INVALIDO, -20004);
    PRAGMA EXCEPTION_INIT(EX_TEXTO_LONGO,     -20005);

    C_LIMITE_TEXTO CONSTANT PLS_INTEGER := 32000;

    v_json     CLOB;
    v_buffer   VARCHAR2(32767);
    v_tipo     VARCHAR2(20);
    v_valor    CLOB;
    v_escalar  VARCHAR2(32767);
    v_campo    VARCHAR2(128);
    v_numero   NUMBER;

    -- Subprograma local: escapa uma string para caber entre aspas em JSON.
    FUNCTION escapar(p_texto IN VARCHAR2) RETURN VARCHAR2 IS
        v_saida VARCHAR2(32767);
        v_char  VARCHAR2(1);
        v_cod   PLS_INTEGER;
    BEGIN
        v_saida := p_texto;
        v_saida := REPLACE(v_saida, CHR(92), CHR(92) || CHR(92));  -- \  primeiro
        v_saida := REPLACE(v_saida, '"',     CHR(92) || '"');      -- "
        v_saida := REPLACE(v_saida, CHR(8),  CHR(92) || 'b');      -- backspace
        v_saida := REPLACE(v_saida, CHR(12), CHR(92) || 'f');      -- form feed
        v_saida := REPLACE(v_saida, CHR(10), CHR(92) || 'n');      -- \n
        v_saida := REPLACE(v_saida, CHR(13), CHR(92) || 'r');      -- \r
        v_saida := REPLACE(v_saida, CHR(9),  CHR(92) || 't');      -- \t

        -- Os demais caracteres de controle são ilegais soltos dentro
        -- de uma string JSON. Viram \u00XX. Sem isto, uma observação colada de
        -- um editor pode gerar JSON que nenhum parser aceita.
        FOR i IN 1 .. 31 LOOP
            IF i NOT IN (8, 9, 10, 12, 13) THEN
                v_char := CHR(i);
                IF INSTR(v_saida, v_char) > 0 THEN
                    v_cod := i;
                    v_saida := REPLACE(v_saida, v_char,
                                CHR(92) || 'u' || LPAD(TO_CHAR(v_cod, 'FM0X'), 4, '0'));
                END IF;
            END IF;
        END LOOP;

        RETURN v_saida;
    END escapar;

    -- Subprograma local: acumula em buffer VARCHAR2 e só desce para o CLOB
    -- quando enche. Concatenar CLOB a cada campo faria uma cópia por vez.
    PROCEDURE emitir(p_texto IN VARCHAR2) IS
    BEGIN
        IF LENGTH(NVL(v_buffer, '')) + LENGTH(p_texto) > 30000 THEN
            DBMS_LOB.WRITEAPPEND(v_json, LENGTH(v_buffer), v_buffer);
            v_buffer := NULL;
        END IF;
        v_buffer := v_buffer || p_texto;
    END emitir;

    -- Subprograma local: anexa um CLOB inteiro sem passar pelo buffer VARCHAR2,
    -- que não comportaria um fragmento grande. Descarrega o buffer antes, para
    -- não trocar a ordem do que já foi emitido.
    PROCEDURE emitir_clob(p_clob IN CLOB) IS
    BEGIN
        IF v_buffer IS NOT NULL THEN
            DBMS_LOB.WRITEAPPEND(v_json, LENGTH(v_buffer), v_buffer);
            v_buffer := NULL;
        END IF;
        IF p_clob IS NOT NULL AND DBMS_LOB.GETLENGTH(p_clob) > 0 THEN
            DBMS_LOB.APPEND(v_json, p_clob);
        END IF;
    END emitir_clob;

BEGIN
    -- EXCEÇÃO 1 — coleção nula ou vazia.
    -- Devolver "{}" seria pior que falhar: a trigger gravaria uma linha de
    -- auditoria sem conteúdo, e ninguém descobriria até precisar dela.
    IF p_pares IS NULL OR p_pares.COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20001,
            'PB_MONTA_JSON: nenhum par (campo, valor, tipo) foi recebido.');
    END IF;

    DBMS_LOB.CREATETEMPORARY(v_json, TRUE);
    emitir('{');

    FOR i IN 1 .. p_pares.COUNT LOOP
        v_campo := p_pares(i).NM_CAMPO;
        v_valor := p_pares(i).VL_VALOR;
        v_tipo  := UPPER(TRIM(p_pares(i).TP_TIPO_CUIDADO));

        -- EXCEÇÃO 2 — campo sem nome.
        -- Um par anônimo produziria `"": valor`, que é JSON válido e inútil.
        IF v_campo IS NULL OR TRIM(v_campo) IS NULL THEN
            RAISE_APPLICATION_ERROR(-20002,
                'PB_MONTA_JSON: par na posição ' || i || ' está sem nome de campo.');
        END IF;

        -- EXCEÇÃO 3 — tipo desconhecido.
        -- Sem esta checagem, um TP_TIPO_CUIDADO errado cairia no ELSE e sairia como
        -- texto, silenciosamente: um número viraria string e o consumidor do
        -- JSON só descobriria ao tentar somar.
        IF v_tipo NOT IN ('TEXTO','NUMERO','BOOLEANO','DATA','NULO','JSON') THEN
            RAISE_APPLICATION_ERROR(-20003,
                'PB_MONTA_JSON: tipo "' || p_pares(i).TP_TIPO_CUIDADO || '" não é reconhecido '
                || 'no campo "' || v_campo || '". Use TEXTO, NUMERO, BOOLEANO, DATA, '
                || 'NULO ou JSON.');
        END IF;

        IF i > 1 THEN
            emitir(',');
        END IF;

        emitir('"' || escapar(v_campo) || '":');

        IF v_valor IS NULL OR v_tipo = 'NULO' THEN
            emitir('null');

        ELSIF v_tipo = 'JSON' THEN
            emitir_clob(v_valor);

        ELSIF v_tipo = 'NUMERO' THEN
            -- EXCEÇÃO 4 — valor não numérico marcado como NUMERO.
            -- Emitir sem validar produziria JSON sintaticamente quebrado
            -- (`"peso":abc`), e o erro apareceria no consumidor, longe daqui.
            v_escalar := DBMS_LOB.SUBSTR(v_valor, C_LIMITE_TEXTO, 1);
            BEGIN
                v_numero := TO_NUMBER(REPLACE(v_escalar, ',', '.'),
                                      '999999999999999999.999999999999',
                                      'NLS_NUMERIC_CHARACTERS = ''.,''');
            EXCEPTION
                WHEN VALUE_ERROR OR INVALID_NUMBER THEN
                    RAISE_APPLICATION_ERROR(-20004,
                        'PB_MONTA_JSON: campo "' || v_campo || '" foi declarado NUMERO '
                        || 'mas recebeu "' || v_escalar || '".');
            END;
            -- FM tira o padding; o NLS explícito garante ponto decimal mesmo se
            -- a sessão estiver configurada com vírgula. JSON não aceita vírgula.
            emitir(TO_CHAR(v_numero, 'FM999999999999999990.999999999999',
                           'NLS_NUMERIC_CHARACTERS = ''.,'''));

        ELSIF v_tipo = 'BOOLEANO' THEN
            -- O schema guarda booleano como NUMBER(1). A trigger e a procedure
            -- passam '1'/'0'; aceitar S/N e TRUE/FALSE evita conversão no
            -- chamador, que é onde o erro ficaria escondido.
            v_escalar := DBMS_LOB.SUBSTR(v_valor, 20, 1);
            IF UPPER(TRIM(v_escalar)) IN ('1','S','T','TRUE','SIM','Y','YES') THEN
                emitir('true');
            ELSE
                emitir('false');
            END IF;

        ELSE
            -- TEXTO e DATA saem entre aspas. A data chega já formatada pelo
            -- chamador — é ele que conhece o formato certo para cada coluna.
            -- EXCEÇÃO 5 — texto além do que o escape processa de uma vez.
            -- Truncar em silêncio seria pior: gravaria auditoria pela metade sem
            -- ninguém perceber. As colunas CLOB grandes do produto
            -- (TX_NARRATIVA, TX_ORIENTACAO) não são usadas por nenhum dos dois
            -- chamadores; se um dia forem, o tratamento vira fatiar aqui.
            IF DBMS_LOB.GETLENGTH(v_valor) > C_LIMITE_TEXTO THEN
                RAISE_APPLICATION_ERROR(-20005,
                    'PB_MONTA_JSON: campo "' || v_campo || '" tem '
                    || DBMS_LOB.GETLENGTH(v_valor) || ' caracteres e excede o limite '
                    || 'de ' || C_LIMITE_TEXTO || ' para TEXTO.');
            END IF;
            v_escalar := DBMS_LOB.SUBSTR(v_valor, C_LIMITE_TEXTO, 1);
            emitir('"' || escapar(v_escalar) || '"');
        END IF;
    END LOOP;

    emitir('}');

    IF v_buffer IS NOT NULL THEN
        DBMS_LOB.WRITEAPPEND(v_json, LENGTH(v_buffer), v_buffer);
    END IF;

    RETURN v_json;

EXCEPTION
    -- Os quatro erros acima já vêm com mensagem própria; repassar sem tocar.
    WHEN OTHERS THEN
        IF SQLCODE BETWEEN -20005 AND -20001 THEN
            RAISE;
        END IF;
        -- EXCEÇÃO 5 — qualquer outra. Sem este bloco, um erro de LOB ou de
        -- memória subiria como ORA-06512 sem dizer de onde veio, e a trigger
        -- que chamou a função abortaria o UPDATE do usuário sem explicação.
        RAISE_APPLICATION_ERROR(-20009,
            'PB_MONTA_JSON: falha inesperada ao montar o JSON — ' || SQLERRM);
END PB_MONTA_JSON;
/


-- TIPO DE APOIO — o que a Função 2 devolve

-- Espelha exatamente as três colunas de decisão que a fusão do ADR s3-24 levou
-- para dentro de T_PB_ITEM_PLANO_CUIDADO: TP_DESFECHO, NR_DOSE_APLICADA e
-- ID_REGRA_APLICADA. Devolver só o número perderia os dois desfechos que não têm
-- dose (SEM_DOSE e ACIONAR_CLINICA), e o chamador teria de recalcular o motivo —
-- que é justamente o que esta função existe para não deixar acontecer duas vezes.

CREATE OR REPLACE TYPE PB_RESULTADO_DOSE AS OBJECT (
    TP_DESFECHO        VARCHAR2(30),
    NR_DOSE_APLICADA   NUMBER(8,3),
    ID_REGRA_APLICADA  NUMBER(10)
);
/


-- =============================================================================
-- FUNÇÃO 2 — PB_DOSE_DO_DIA
-- =============================================================================
-- Substitui o processo lógico central do produto: a **verificação de limites**
-- que decide a dose de hoje.
--
-- Dada uma prescrição (faixa NR_DOSE_MIN–NR_DOSE_MAX) e um check-in do tutor
-- (as condições que a IA extraiu do relato), devolve a dose — ou a decisão de
-- suspender ou acionar a clínica.
--
-- É determinística por decisão de produto: quem decide a dose é a
-- regra assinada pela veterinária, NUNCA a IA. A IA só extrai o valor da
-- condição a partir do texto; a comparação é aritmética.
--
--
-- Exceções tratadas: 5 (quatro nomeadas + OTHERS).
CREATE OR REPLACE FUNCTION PB_DOSE_DO_DIA (
    p_id_prescricao          IN NUMBER,
    p_id_checkin_tratamento  IN NUMBER
) RETURN PB_RESULTADO_DOSE IS

    EX_PRESCRICAO_INEXISTENTE  EXCEPTION;
    EX_CHECKIN_INEXISTENTE     EXCEPTION;
    EX_FONTE_NAO_AVALIAVEL     EXCEPTION;
    EX_VALOR_INCOMPATIVEL      EXCEPTION;
    PRAGMA EXCEPTION_INIT(EX_PRESCRICAO_INEXISTENTE, -20011);
    PRAGMA EXCEPTION_INIT(EX_CHECKIN_INEXISTENTE,    -20012);
    PRAGMA EXCEPTION_INIT(EX_FONTE_NAO_AVALIAVEL,    -20013);
    PRAGMA EXCEPTION_INIT(EX_VALOR_INCOMPATIVEL,     -20014);

    v_dose_min     T_PB_PRESCRICAO.NR_DOSE_MIN%TYPE;
    v_dose_max     T_PB_PRESCRICAO.NR_DOSE_MAX%TYPE;
    v_existe       NUMBER;

    v_casou        BOOLEAN;
    v_tem_valor    BOOLEAN;
    v_bool         NUMBER;
    v_num          NUMBER;

BEGIN
    -- EXCEÇÃO 1 — prescrição inexistente.
    -- Sem esta checagem o SELECT abaixo levantaria NO_DATA_FOUND cru, e o
    -- chamador receberia ORA-01403 sem saber qual dos dois ids estava errado.
    BEGIN
        SELECT NR_DOSE_MIN, NR_DOSE_MAX
          INTO v_dose_min, v_dose_max
          FROM T_PB_PRESCRICAO
         WHERE ID_PRESCRICAO = p_id_prescricao;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20011,
                'PB_DOSE_DO_DIA: prescrição ' || p_id_prescricao || ' não existe.');
    END;

    -- EXCEÇÃO 2 — check-in inexistente.
    -- Este caso é pior que o anterior se passar batido: sem o cabeçalho, a
    -- consulta de extrações volta vazia, nenhuma regra casa, e a função
    -- devolveria a dose padrão como se o tutor tivesse relatado "tudo bem".
    -- Silenciosamente medicando com base em relato que nunca existiu.
    SELECT COUNT(*) INTO v_existe
      FROM T_PB_CHECKIN
     WHERE ID_CHECKIN = p_id_checkin_tratamento;

    IF v_existe = 0 THEN
        RAISE_APPLICATION_ERROR(-20012,
            'PB_DOSE_DO_DIA: check-in ' || p_id_checkin_tratamento || ' não existe.');
    END IF;

    -- As regras da prescrição, na ordem que a veterinária assinou.
    FOR r IN (SELECT ID_REGRA_PRESCRICAO, ID_CONDICAO_CLINICA, TP_DADO_CONGELADO,
                     TP_FONTE_VALOR_CONGELADA, TP_OPERADOR, NR_LIMITE, TP_ACAO_DOSE, NR_ORDEM
                FROM T_PB_REGRA_PRESCRICAO
               WHERE ID_PRESCRICAO = p_id_prescricao
               ORDER BY NR_ORDEM)
    LOOP
        -- EXCEÇÃO 3 — regra apontando para fonte que a Sprint 3 não avalia.
        -- A fonte é lida da REGRA (TP_FONTE_VALOR_CONGELADA), nunca por join com o
        -- catálogo: se a clínica trocar a fonte de uma condição depois da
        -- assinatura, a regra assinada não pode mudar de comportamento.
        --
        -- Só RELATO tem caminho de avaliação na S3 — é a extração do check-in
        -- que preenche o valor. ANIMAL (peso, idade) e HISTORICO (acumulado)
        -- existem no enum para o motor já nascer lendo a fonte, mas ninguém
        -- preenche o valor delas ainda. Falhar alto é melhor que ignorar a
        -- regra em silêncio e medicar pela dose padrão.
        IF r.TP_FONTE_VALOR_CONGELADA <> 'RELATO' THEN
            RAISE_APPLICATION_ERROR(-20013,
                'PB_DOSE_DO_DIA: regra ' || r.ID_REGRA_PRESCRICAO || ' usa fonte '
                || r.TP_FONTE_VALOR_CONGELADA || ', que não é avaliável nesta sprint. '
                || 'Apenas RELATO tem valor preenchido pela extração do check-in.');
        END IF;

        v_casou     := FALSE;
        v_tem_valor := FALSE;
        v_bool      := NULL;
        v_num       := NULL;

        -- O que a IA extraiu para esta condição, neste check-in.
        -- Condição não relatada simplesmente não casa — não é erro: o tutor não
        -- falou sobre ela.
        BEGIN
            SELECT BL_VALOR_BOOLEANO, NR_VALOR_NUMERICO
              INTO v_bool, v_num
              FROM T_PB_CONDICAO_OBSERVADA
             WHERE ID_CHECKIN = p_id_checkin_tratamento
               AND ID_CONDICAO_CLINICA   = r.ID_CONDICAO_CLINICA;
            v_tem_valor := TRUE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_tem_valor := FALSE;
            WHEN TOO_MANY_ROWS THEN
                -- Duas linhas para a mesma condição no mesmo relato: a extração
                -- se contradisse. Não há unicidade no schema que impeça, e
                -- escolher uma das duas seria inventar.
                RAISE_APPLICATION_ERROR(-20014,
                    'PB_DOSE_DO_DIA: o check-in ' || p_id_checkin_tratamento
                    || ' tem mais de uma extração para a condição '
                    || r.ID_CONDICAO_CLINICA || '.');
        END;

        IF v_tem_valor THEN
            IF r.TP_DADO_CONGELADO = 'BOOLEANO' THEN
                -- EXCEÇÃO 4a — regra booleana contra extração sem booleano.
                IF v_bool IS NULL THEN
                    RAISE_APPLICATION_ERROR(-20014,
                        'PB_DOSE_DO_DIA: regra ' || r.ID_REGRA_PRESCRICAO
                        || ' é booleana, mas a extração da condição '
                        || r.ID_CONDICAO_CLINICA || ' não trouxe valor booleano.');
                END IF;
                -- Condição booleana casa quando é verdadeira. "Se vomitou."
                v_casou := (v_bool = 1);

            ELSE
                -- EXCEÇÃO 4b — regra numérica contra extração sem número.
                IF v_num IS NULL THEN
                    RAISE_APPLICATION_ERROR(-20014,
                        'PB_DOSE_DO_DIA: regra ' || r.ID_REGRA_PRESCRICAO
                        || ' é numérica, mas a extração da condição '
                        || r.ID_CONDICAO_CLINICA || ' não trouxe valor numérico.');
                END IF;

                v_casou := CASE r.TP_OPERADOR
                               WHEN 'MAIOR_QUE'      THEN v_num >  r.NR_LIMITE
                               WHEN 'MAIOR_OU_IGUAL' THEN v_num >= r.NR_LIMITE
                               WHEN 'MENOR_QUE'      THEN v_num <  r.NR_LIMITE
                               WHEN 'MENOR_OU_IGUAL' THEN v_num <= r.NR_LIMITE
                               WHEN 'IGUAL'          THEN v_num =  r.NR_LIMITE
                           END;
                -- TP_OPERADOR e NR_LIMITE são NOT NULL quando o valor é
                -- NUMERICO — CK_REGRA_COERENCIA garante no banco. O CASE acima
                -- não tem ELSE porque CK_REGRA_OPERADOR já fecha o domínio.
            END IF;
        END IF;

        -- Primeira que casa vence: retorna aqui, sem olhar o resto.
        IF v_casou THEN
            RETURN CASE r.TP_ACAO_DOSE
                WHEN 'DOSE_MIN' THEN
                    PB_RESULTADO_DOSE('DOSE_CALCULADA', v_dose_min, r.ID_REGRA_PRESCRICAO)
                WHEN 'DOSE_MAX' THEN
                    PB_RESULTADO_DOSE('DOSE_CALCULADA', v_dose_max, r.ID_REGRA_PRESCRICAO)
                WHEN 'DOSE_PADRAO' THEN
                    PB_RESULTADO_DOSE('DOSE_CALCULADA',
                                      ROUND((v_dose_min + v_dose_max) / 2, 3),
                                      r.ID_REGRA_PRESCRICAO)
                WHEN 'ACIONAR_CLINICA' THEN
                    PB_RESULTADO_DOSE('ACIONAR_CLINICA', NULL, r.ID_REGRA_PRESCRICAO)
                WHEN 'SUSPENDER' THEN
                    PB_RESULTADO_DOSE('SEM_DOSE', NULL, r.ID_REGRA_PRESCRICAO)
            END;
        END IF;
    END LOOP;

    -- Nenhuma regra casou: o dia foi normal, dose padrão, sem regra a citar.
    RETURN PB_RESULTADO_DOSE('DOSE_CALCULADA',
                             ROUND((v_dose_min + v_dose_max) / 2, 3),
                             NULL);

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE BETWEEN -20014 AND -20011 THEN
            RAISE;
        END IF;
        -- EXCEÇÃO 5 — qualquer outra. Numa função que decide dose de remédio,
        -- erro engolido é o pior desfecho possível: o chamador receberia NULL e
        -- poderia interpretar como "sem dose hoje".
        RAISE_APPLICATION_ERROR(-20019,
            'PB_DOSE_DO_DIA: falha inesperada ao calcular a dose — ' || SQLERRM);
END PB_DOSE_DO_DIA;
/