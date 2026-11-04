-- =============================================================================
-- 05 — PACOTES
-- =============================================================================
-- Funções, procedimentos e a lógica da trigger agrupados por assunto:
--
--   PKG_PB_JSON        Função 1 (MONTA_JSON) e Procedimento 1 (PLANO_JSON):
--                      tudo o que transforma linhas relacionais em JSON.
--   PKG_PB_TRATAMENTO  Função 2 (DOSE_DO_DIA) e Procedimento 2
--                      (ADESAO_POR_DESFECHO): o tratamento em casa — a dose de
--                      cada dia e o relatório de adesão.
--   PKG_PB_AUDITORIA   A lógica da trigger PB_AUDITA_CONDICAO (bloco 06): monta
--                      o JSON da condição e grava a linha de auditoria.
--
-- A especificação de cada pacote é o contrato: só o que está nela é chamável de
-- fora. Os subprogramas auxiliares (escapar, imprimir, linha, cabecalho) ficam
-- só no corpo, privados.
--
-- Faixas de erro, por pacote, para quem lê um ORA-20xxx saber de onde veio:
--   -20001 a -20009  PKG_PB_JSON.MONTA_JSON
--   -20011 a -20019  PKG_PB_TRATAMENTO.DOSE_DO_DIA
--   -20021 a -20029  PKG_PB_JSON.PLANO_JSON
--   -20031 a -20039  PKG_PB_TRATAMENTO.ADESAO_POR_DESFECHO
--   -20041 a -20049  PKG_PB_AUDITORIA e a trigger
-- =============================================================================


-- =============================================================================
-- PKG_PB_JSON — especificação
-- =============================================================================
CREATE OR REPLACE PACKAGE PKG_PB_JSON AS

    -- FUNÇÃO 1 — recebe pares relacionais e devolve um objeto JSON como CLOB.
    FUNCTION MONTA_JSON (
        p_pares IN PB_PARES_JSON
    ) RETURN CLOB;

    -- PROCEDIMENTO 1 — o plano de cuidado em JSON, num JOIN de três tabelas:
    -- T_PB_ANIMAL x T_PB_PLANO_CUIDADO x T_PB_ITEM_PLANO_CUIDADO.
    --
    -- Devolve em p_json um ARRAY com um documento por plano, os eventos
    -- embutidos, e também imprime o array (DBMS_OUTPUT) para a demonstração.
    --
    -- p_id_animal informado: os planos daquele animal.
    -- p_id_animal NULO:      o dataset inteiro, todos os animais com plano — é o
    --                        arquivo que a Sprint 4 importa no MongoDB, gerado
    --                        pela rota GET /api/exportacao/planos-cuidado do Java.
    PROCEDURE PLANO_JSON (
        p_id_animal IN  NUMBER,
        p_json      OUT NOCOPY CLOB
    );

    -- Atalho para o SQL Developer: o mesmo procedimento, só imprimindo.
    PROCEDURE PLANO_JSON (
        p_id_animal IN NUMBER
    );

END PKG_PB_JSON;
/


-- =============================================================================
-- PKG_PB_JSON — corpo
-- =============================================================================
CREATE OR REPLACE PACKAGE BODY PKG_PB_JSON AS

    -- Imprime um CLOB em pedaços. DBMS_OUTPUT.PUT_LINE não aceita mais de 32767
    -- caracteres de uma vez, e um plano com muitos eventos passa disso.
    -- Privado: só o Procedimento 1 usa.
    PROCEDURE imprimir(p_clob IN CLOB) IS
        C_PEDACO CONSTANT PLS_INTEGER := 8000;
        v_pos    PLS_INTEGER := 1;
        v_total  PLS_INTEGER := NVL(DBMS_LOB.GETLENGTH(p_clob), 0);
    BEGIN
        WHILE v_pos <= v_total LOOP
            DBMS_OUTPUT.PUT_LINE(DBMS_LOB.SUBSTR(p_clob, C_PEDACO, v_pos));
            v_pos := v_pos + C_PEDACO;
        END LOOP;
    END imprimir;


    -- =========================================================================
    -- FUNÇÃO 1 — MONTA_JSON
    -- =========================================================================
    -- Exceções tratadas: 6 (cinco nomeadas + OTHERS).
    FUNCTION MONTA_JSON (
        p_pares IN PB_PARES_JSON
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
                'MONTA_JSON: nenhum par (campo, valor, tipo) foi recebido.');
        END IF;

        DBMS_LOB.CREATETEMPORARY(v_json, TRUE);
        emitir('{');

        FOR i IN 1 .. p_pares.COUNT LOOP
            v_campo := p_pares(i).NM_CAMPO;
            v_valor := p_pares(i).VL_VALOR;
            v_tipo  := UPPER(TRIM(p_pares(i).TP_TIPO));

            -- EXCEÇÃO 2 — campo sem nome.
            -- Um par anônimo produziria `"": valor`, que é JSON válido e inútil.
            IF v_campo IS NULL OR TRIM(v_campo) IS NULL THEN
                RAISE_APPLICATION_ERROR(-20002,
                    'MONTA_JSON: par na posição ' || i || ' está sem nome de campo.');
            END IF;

            -- EXCEÇÃO 3 — tipo desconhecido.
            -- Sem esta checagem, um TP_TIPO errado cairia no ELSE e sairia como
            -- texto, silenciosamente: um número viraria string e o consumidor do
            -- JSON só descobriria ao tentar somar.
            IF v_tipo IS NULL OR v_tipo NOT IN ('TEXTO','NUMERO','BOOLEANO','DATA','NULO','JSON') THEN
                RAISE_APPLICATION_ERROR(-20003,
                    'MONTA_JSON: tipo "' || p_pares(i).TP_TIPO || '" não é reconhecido '
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
                            'MONTA_JSON: campo "' || v_campo || '" foi declarado NUMERO '
                            || 'mas recebeu "' || v_escalar || '".');
                END;
                -- FM tira o padding; o NLS explícito garante ponto decimal mesmo se
                -- a sessão estiver configurada com vírgula. JSON não aceita vírgula.
                emitir(TO_CHAR(v_numero, 'FM999999999999999990.999999999999',
                               'NLS_NUMERIC_CHARACTERS = ''.,'''));

            ELSIF v_tipo = 'BOOLEANO' THEN
                -- O schema guarda booleano como NUMBER(1). A trigger e o
                -- procedimento passam '1'/'0'; aceitar S/N e TRUE/FALSE evita
                -- conversão no chamador, que é onde o erro ficaria escondido.
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
                -- ninguém perceber.
                IF DBMS_LOB.GETLENGTH(v_valor) > C_LIMITE_TEXTO THEN
                    RAISE_APPLICATION_ERROR(-20005,
                        'MONTA_JSON: campo "' || v_campo || '" tem '
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
        -- Os cinco erros acima já vêm com mensagem própria; repassar sem tocar.
        WHEN OTHERS THEN
            IF SQLCODE BETWEEN -20005 AND -20001 THEN
                RAISE;
            END IF;
            -- EXCEÇÃO 6 — qualquer outra. Sem este bloco, um erro de LOB ou de
            -- memória subiria como ORA-06512 sem dizer de onde veio, e a trigger
            -- que chamou a função abortaria o UPDATE do usuário sem explicação.
            RAISE_APPLICATION_ERROR(-20009,
                'MONTA_JSON: falha inesperada ao montar o JSON — ' || SQLERRM);
    END MONTA_JSON;


    -- =========================================================================
    -- PROCEDIMENTO 1 — PLANO_JSON
    -- =========================================================================
    -- UM cursor, com JOIN explícito das três tabelas:
    --
    --   T_PB_ANIMAL  ⟕  T_PB_PLANO_CUIDADO  ⟕  T_PB_ITEM_PLANO_CUIDADO
    --
    -- Cada linha do JOIN é um item do plano, com os dados do animal e do plano
    -- repetidos. O documento é montado por QUEBRA DE CONTROLE: enquanto o plano
    -- é o mesmo, o item entra no array de eventos; quando o plano muda, o
    -- documento anterior é fechado. Por isso o ORDER BY começa por animal e
    -- plano — a quebra só funciona com as linhas do mesmo plano juntas.
    --
    -- LEFT JOIN, e não INNER, de propósito: com INNER, um animal sem plano e um
    -- plano sem itens simplesmente sumiriam do resultado, e as exceções 2 e 3
    -- abaixo — que são justamente esses dois casos — nunca disparariam. Com LEFT,
    -- eles chegam como linhas com o lado direito nulo, e o laço decide.
    --
    -- FORMA DO DOCUMENTO: os eventos vêm EMBUTIDOS num array dentro do plano,
    -- não em linhas repetidas com os dados do animal duplicados. É o que a
    -- Sprint 4 importa no MongoDB — um documento por plano, com a lista dentro,
    -- que é a modelagem natural de um banco de documentos.
    --
    -- Exceções tratadas: 5 (quatro nomeadas + OTHERS).
    PROCEDURE PLANO_JSON (
        p_id_animal IN  NUMBER,
        p_json      OUT NOCOPY CLOB
    ) IS
        EX_ANIMAL_INEXISTENTE  EXCEPTION;
        EX_SEM_PLANO           EXCEPTION;
        EX_PLANO_SEM_EVENTOS   EXCEPTION;
        EX_DATASET_VAZIO       EXCEPTION;
        PRAGMA EXCEPTION_INIT(EX_ANIMAL_INEXISTENTE, -20021);
        PRAGMA EXCEPTION_INIT(EX_SEM_PLANO,          -20022);
        PRAGMA EXCEPTION_INIT(EX_PLANO_SEM_EVENTOS,  -20023);
        PRAGMA EXCEPTION_INIT(EX_DATASET_VAZIO,      -20024);

        -- Cabeçalho do plano aberto: vem da primeira linha do JOIN de cada plano
        -- e é usado quando o plano fecha.
        v_plano_atual  T_PB_PLANO_CUIDADO.ID_PLANO_CUIDADO%TYPE := NULL;
        v_cab_animal   T_PB_ANIMAL.ID_ANIMAL%TYPE;
        v_cab_nome     T_PB_ANIMAL.NM_NOME_ANIMAL%TYPE;
        v_cab_especie  T_PB_ANIMAL.ES_ESPECIE%TYPE;
        v_cab_raca     T_PB_ANIMAL.RC_RACA%TYPE;
        v_cab_porte    T_PB_ANIMAL.PT_PORTE%TYPE;
        v_cab_nasc     T_PB_ANIMAL.DT_DATA_NASCIMENTO%TYPE;
        v_cab_peso     T_PB_ANIMAL.NR_PESO%TYPE;
        v_cab_protoc   T_PB_PLANO_CUIDADO.ID_PROTOCOLO%TYPE;
        v_cab_consulta T_PB_PLANO_CUIDADO.ID_CONSULTA%TYPE;
        v_cab_categ    T_PB_PLANO_CUIDADO.TP_CATEGORIA_PLANO%TYPE;
        v_cab_status   T_PB_PLANO_CUIDADO.ST_STATUS_PLANO%TYPE;
        v_cab_criado   T_PB_PLANO_CUIDADO.CA_CREATED_AT%TYPE;

        v_linhas     PLS_INTEGER := 0;   -- linhas que o JOIN devolveu
        v_planos     PLS_INTEGER := 0;   -- documentos emitidos
        v_eventos    PLS_INTEGER := 0;   -- eventos do plano aberto

        v_array  CLOB;   -- eventos do plano aberto
        v_doc    CLOB;

        -- Fecha o plano aberto: monta o documento e o acrescenta ao array de saída.
        PROCEDURE fechar_plano IS
        BEGIN
            DBMS_LOB.WRITEAPPEND(v_array, 1, ']');

            v_doc := MONTA_JSON(PB_PARES_JSON(
                PB_PAR_JSON('planoId',      TO_CHAR(v_plano_atual),                     'NUMERO'),
                PB_PAR_JSON('categoria',    v_cab_categ,                                'TEXTO'),
                PB_PAR_JSON('status',       v_cab_status,                               'TEXTO'),
                PB_PAR_JSON('protocoloId',  TO_CHAR(v_cab_protoc),                      'NUMERO'),
                PB_PAR_JSON('consultaId',   TO_CHAR(v_cab_consulta),                    'NUMERO'),
                PB_PAR_JSON('criadoEm',     TO_CHAR(v_cab_criado,
                                                    'YYYY-MM-DD"T"HH24:MI:SS'),         'DATA'),
                PB_PAR_JSON('animalId',     TO_CHAR(v_cab_animal),                      'NUMERO'),
                PB_PAR_JSON('animalNome',   v_cab_nome,                                 'TEXTO'),
                PB_PAR_JSON('especie',      v_cab_especie,                              'TEXTO'),
                PB_PAR_JSON('raca',         v_cab_raca,                                 'TEXTO'),
                PB_PAR_JSON('porte',        v_cab_porte,                                'TEXTO'),
                PB_PAR_JSON('nascimento',   TO_CHAR(v_cab_nasc, 'YYYY-MM-DD'),          'DATA'),
                PB_PAR_JSON('pesoKg',       TO_CHAR(v_cab_peso),                        'NUMERO'),
                PB_PAR_JSON('totalEventos', TO_CHAR(v_eventos),                         'NUMERO'),
                -- O array entra CRU. É este par que justifica o tipo JSON existir.
                PB_PAR_JSON('eventos',      v_array,                                    'JSON')
            ));

            IF v_planos > 0 THEN
                DBMS_LOB.WRITEAPPEND(p_json, 1, ',');
            END IF;
            DBMS_LOB.APPEND(p_json, v_doc);
            v_planos := v_planos + 1;

            DBMS_LOB.FREETEMPORARY(v_array);
            v_plano_atual := NULL;
        END fechar_plano;

    BEGIN
        DBMS_LOB.CREATETEMPORARY(p_json, TRUE);
        DBMS_LOB.WRITEAPPEND(p_json, 1, '[');

        FOR r IN (
            SELECT a.ID_ANIMAL,
                   a.NM_NOME_ANIMAL,
                   a.ES_ESPECIE,
                   a.RC_RACA,
                   a.PT_PORTE,
                   a.DT_DATA_NASCIMENTO,
                   a.NR_PESO,
                   pl.ID_PLANO_CUIDADO,
                   pl.ID_PROTOCOLO,
                   pl.ID_CONSULTA,
                   pl.TP_CATEGORIA_PLANO,
                   pl.ST_STATUS_PLANO,
                   pl.CA_CREATED_AT         AS PLANO_CRIADO_EM,
                   it.ID_ITEM_PLANO_CUIDADO,
                   it.TP_ORIGEM,
                   it.TP_TIPO_CUIDADO,
                   it.NM_NOME               AS ITEM_NOME,
                   it.DT_DATA_ALVO,
                   it.ST_STATUS_ITEM,
                   it.ID_PRESCRICAO,
                   it.ID_PROCEDIMENTO,
                   it.DT_EXECUTADO_EM
              FROM T_PB_ANIMAL a
              LEFT JOIN T_PB_PLANO_CUIDADO pl
                ON pl.ID_ANIMAL = a.ID_ANIMAL
              LEFT JOIN T_PB_ITEM_PLANO_CUIDADO it
                ON it.ID_PLANO_CUIDADO = pl.ID_PLANO_CUIDADO
             WHERE p_id_animal IS NULL
                OR a.ID_ANIMAL = p_id_animal
             ORDER BY a.ID_ANIMAL, pl.ID_PLANO_CUIDADO,
                      it.DT_DATA_ALVO, it.ID_ITEM_PLANO_CUIDADO)
        LOOP
            v_linhas := v_linhas + 1;

            -- Animal sem nenhum plano: o LEFT JOIN trouxe o animal com o plano nulo.
            IF r.ID_PLANO_CUIDADO IS NULL THEN
                -- EXCEÇÃO 2 — pedido para um animal só, e ele não tem plano.
                -- Separada da exceção 1 de propósito: "o id está errado" e "o motor
                -- nunca criou plano para este pet" são problemas diferentes.
                IF p_id_animal IS NOT NULL THEN
                    RAISE_APPLICATION_ERROR(-20022,
                        'PLANO_JSON: o animal ' || p_id_animal || ' (' || r.NM_NOME_ANIMAL
                        || ') existe, mas não tem plano de cuidado.');
                END IF;
                -- No dataset inteiro, animal sem plano não é erro: só não gera documento.
                CONTINUE;
            END IF;

            -- QUEBRA DE CONTROLE — mudou o plano: fecha o anterior e abre este.
            IF v_plano_atual IS NULL OR r.ID_PLANO_CUIDADO <> v_plano_atual THEN
                IF v_plano_atual IS NOT NULL THEN
                    fechar_plano;
                END IF;

                -- EXCEÇÃO 3 — plano sem nenhum evento: o LEFT JOIN trouxe o plano
                -- com o item nulo. Não é caso de borda inofensivo: um plano vazio
                -- quer dizer que a instanciação falhou no meio, e o tutor abriria o
                -- app com um checklist em branco sem nada indicando erro.
                IF r.ID_ITEM_PLANO_CUIDADO IS NULL THEN
                    RAISE_APPLICATION_ERROR(-20023,
                        'PLANO_JSON: plano ' || r.ID_PLANO_CUIDADO
                        || ' do animal ' || r.ID_ANIMAL || ' não tem nenhum evento. '
                        || 'A instanciação do plano não terminou.');
                END IF;

                v_plano_atual  := r.ID_PLANO_CUIDADO;
                v_cab_animal   := r.ID_ANIMAL;
                v_cab_nome     := r.NM_NOME_ANIMAL;
                v_cab_especie  := r.ES_ESPECIE;
                v_cab_raca     := r.RC_RACA;
                v_cab_porte    := r.PT_PORTE;
                v_cab_nasc     := r.DT_DATA_NASCIMENTO;
                v_cab_peso     := r.NR_PESO;
                v_cab_protoc   := r.ID_PROTOCOLO;
                v_cab_consulta := r.ID_CONSULTA;
                v_cab_categ    := r.TP_CATEGORIA_PLANO;
                v_cab_status   := r.ST_STATUS_PLANO;
                v_cab_criado   := r.PLANO_CRIADO_EM;
                v_eventos      := 0;

                DBMS_LOB.CREATETEMPORARY(v_array, TRUE);
                DBMS_LOB.WRITEAPPEND(v_array, 1, '[');
            END IF;

            -- O item desta linha vira um objeto do array de eventos do plano aberto.
            v_eventos := v_eventos + 1;
            IF v_eventos > 1 THEN
                DBMS_LOB.WRITEAPPEND(v_array, 1, ',');
            END IF;

            DBMS_LOB.APPEND(v_array, MONTA_JSON(PB_PARES_JSON(
                PB_PAR_JSON('id',             TO_CHAR(r.ID_ITEM_PLANO_CUIDADO),        'NUMERO'),
                PB_PAR_JSON('origem',         r.TP_ORIGEM,                             'TEXTO'),
                PB_PAR_JSON('tipo',           r.TP_TIPO_CUIDADO,                       'TEXTO'),
                PB_PAR_JSON('nome',           r.ITEM_NOME,                             'TEXTO'),
                PB_PAR_JSON('dataAlvo',       TO_CHAR(r.DT_DATA_ALVO, 'YYYY-MM-DD'),   'DATA'),
                PB_PAR_JSON('status',         r.ST_STATUS_ITEM,                        'TEXTO'),
                -- "quem faz o item" é DERIVADO de TP_ORIGEM, não é coluna:
                -- PRESCRICAO é em casa, PROTOCOLO é na clínica.
                PB_PAR_JSON('feitoEmCasa',    CASE r.TP_ORIGEM WHEN 'PRESCRICAO' THEN '1'
                                                               ELSE '0' END,           'BOOLEANO'),
                PB_PAR_JSON('prescricaoId',   TO_CHAR(r.ID_PRESCRICAO),                'NUMERO'),
                PB_PAR_JSON('procedimentoId', TO_CHAR(r.ID_PROCEDIMENTO),              'NUMERO'),
                PB_PAR_JSON('executadoEm',    TO_CHAR(r.DT_EXECUTADO_EM,
                                                      'YYYY-MM-DD"T"HH24:MI:SS'),      'DATA')
            )));
        END LOOP;

        -- O último plano não tem quem o feche pela quebra, porque o laço acabou.
        IF v_plano_atual IS NOT NULL THEN
            fechar_plano;
        END IF;

        -- EXCEÇÃO 1 — animal inexistente: o JOIN parte de T_PB_ANIMAL, então
        -- nenhuma linha com o filtro por id quer dizer que o id não existe.
        IF v_linhas = 0 AND p_id_animal IS NOT NULL THEN
            RAISE_APPLICATION_ERROR(-20021,
                'PLANO_JSON: animal ' || p_id_animal || ' não existe.');
        END IF;

        -- EXCEÇÃO 4 — dataset inteiro sem nenhum plano. Exportar "[]" para o
        -- MongoDB importaria uma coleção vazia sem ninguém notar.
        IF v_planos = 0 THEN
            RAISE_APPLICATION_ERROR(-20024,
                'PLANO_JSON: nenhum animal tem plano de cuidado; não há dataset para exportar.');
        END IF;

        DBMS_LOB.WRITEAPPEND(p_json, 1, ']');
        imprimir(p_json);

    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE BETWEEN -20024 AND -20021 THEN
                RAISE;
            END IF;
            -- EXCEÇÃO 5 — inclui os erros que vêm de dentro da Função 1
            -- (-20001 a -20005 e -20009). Repassar com o contexto de quem chamou
            -- evita a caça ao tesouro entre procedimento e função.
            RAISE_APPLICATION_ERROR(-20029,
                'PLANO_JSON: falha ao montar o plano'
                || CASE WHEN p_id_animal IS NOT NULL
                        THEN ' do animal ' || p_id_animal ELSE 's' END
                || ' — ' || SQLERRM);
    END PLANO_JSON;


    -- Atalho: mesmo procedimento, sem o parâmetro de saída.
    PROCEDURE PLANO_JSON (
        p_id_animal IN NUMBER
    ) IS
        v_json CLOB;
    BEGIN
        PLANO_JSON(p_id_animal, v_json);
    END PLANO_JSON;

END PKG_PB_JSON;
/


-- =============================================================================
-- PKG_PB_TRATAMENTO — especificação
-- =============================================================================
CREATE OR REPLACE PACKAGE PKG_PB_TRATAMENTO AS

    -- FUNÇÃO 2 — a verificação de limites que decide a dose de hoje.
    FUNCTION DOSE_DO_DIA (
        p_id_prescricao          IN NUMBER,
        p_id_checkin_tratamento  IN NUMBER
    ) RETURN PB_RESULTADO_DOSE;

    -- PROCEDIMENTO 2 — adesão ao tratamento por medicamento e desfecho, com
    -- subtotal e total geral somados manualmente.
    PROCEDURE ADESAO_POR_DESFECHO (
        p_data_inicio IN DATE DEFAULT NULL,
        p_data_fim    IN DATE DEFAULT NULL
    );

END PKG_PB_TRATAMENTO;
/


-- =============================================================================
-- PKG_PB_TRATAMENTO — corpo
-- =============================================================================
CREATE OR REPLACE PACKAGE BODY PKG_PB_TRATAMENTO AS

    -- Larguras da listagem do Procedimento 2, num lugar só. Ficam no topo do
    -- corpo porque o PL/SQL exige declarar constantes antes dos subprogramas.
    C_W_MED  CONSTANT PLS_INTEGER := 24;
    C_W_DES  CONSTANT PLS_INTEGER := 18;

    -- =========================================================================
    -- FUNÇÃO 2 — DOSE_DO_DIA
    -- =========================================================================
    -- Substitui o processo lógico central do produto: a verificação de limites
    -- que decide a dose de hoje.
    --
    -- Dada uma prescrição (faixa NR_DOSE_MIN–NR_DOSE_MAX) e um check-in do tutor
    -- (as condições que a IA extraiu do relato), devolve a dose — ou a decisão de
    -- suspender ou acionar a clínica.
    --
    -- É determinística por decisão de produto: quem decide a dose é a regra
    -- assinada pela veterinária, NUNCA a IA. A IA só extrai o valor da condição
    -- a partir do texto; a comparação é aritmética.
    --
    -- Exceções tratadas: 5 (quatro nomeadas + OTHERS).
    FUNCTION DOSE_DO_DIA (
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
                    'DOSE_DO_DIA: prescrição ' || p_id_prescricao || ' não existe.');
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
                'DOSE_DO_DIA: check-in ' || p_id_checkin_tratamento || ' não existe.');
        END IF;

        -- As regras da prescrição, na ordem que a veterinária assinou.
        FOR r IN (SELECT ID_REGRA_PRESCRICAO, ID_CONDICAO_CLINICA, TP_DADO_CONGELADO,
                         TP_FONTE_VALOR_CONGELADA, TP_OPERADOR, NR_LIMITE, TP_ACAO_DOSE, NR_ORDEM
                    FROM T_PB_REGRA_PRESCRICAO
                   WHERE ID_PRESCRICAO = p_id_prescricao
                   ORDER BY NR_ORDEM)
        LOOP
            -- EXCEÇÃO 3 — regra apontando para fonte que ainda não é avaliada.
            -- A fonte é lida da REGRA (TP_FONTE_VALOR_CONGELADA), nunca por join
            -- com o catálogo: se a clínica trocar a fonte de uma condição depois
            -- da assinatura, a regra assinada não pode mudar de comportamento.
            --
            -- Só RELATO tem caminho de avaliação — é a extração do check-in que
            -- preenche o valor. ANIMAL (peso, idade) e HISTORICO (acumulado)
            -- existem no enum para o motor já nascer lendo a fonte, mas ninguém
            -- preenche o valor delas ainda. Falhar alto é melhor que ignorar a
            -- regra em silêncio e medicar pela dose padrão.
            IF r.TP_FONTE_VALOR_CONGELADA <> 'RELATO' THEN
                RAISE_APPLICATION_ERROR(-20013,
                    'DOSE_DO_DIA: regra ' || r.ID_REGRA_PRESCRICAO || ' usa fonte '
                    || r.TP_FONTE_VALOR_CONGELADA || ', que ainda não é avaliável. '
                    || 'Apenas RELATO tem valor preenchido pela extração do check-in.');
            END IF;

            v_casou     := FALSE;
            v_tem_valor := FALSE;
            v_bool      := NULL;
            v_num       := NULL;

            -- O que a IA extraiu para esta condição, neste check-in.
            -- Condição não relatada simplesmente não casa — não é erro: o tutor
            -- não falou sobre ela.
            BEGIN
                SELECT BL_VALOR_BOOLEANO, NR_VALOR_NUMERICO
                  INTO v_bool, v_num
                  FROM T_PB_CONDICAO_OBSERVADA
                 WHERE ID_CHECKIN          = p_id_checkin_tratamento
                   AND ID_CONDICAO_CLINICA = r.ID_CONDICAO_CLINICA;
                v_tem_valor := TRUE;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    v_tem_valor := FALSE;
                WHEN TOO_MANY_ROWS THEN
                    -- Duas linhas para a mesma condição no mesmo relato: a
                    -- extração se contradisse. Não há unicidade no schema que
                    -- impeça, e escolher uma das duas seria inventar.
                    RAISE_APPLICATION_ERROR(-20014,
                        'DOSE_DO_DIA: o check-in ' || p_id_checkin_tratamento
                        || ' tem mais de uma extração para a condição '
                        || r.ID_CONDICAO_CLINICA || '.');
            END;

            IF v_tem_valor THEN
                IF r.TP_DADO_CONGELADO = 'BOOLEANO' THEN
                    -- EXCEÇÃO 4a — regra booleana contra extração sem booleano.
                    IF v_bool IS NULL THEN
                        RAISE_APPLICATION_ERROR(-20014,
                            'DOSE_DO_DIA: regra ' || r.ID_REGRA_PRESCRICAO
                            || ' é booleana, mas a extração da condição '
                            || r.ID_CONDICAO_CLINICA || ' não trouxe valor booleano.');
                    END IF;
                    -- Condição booleana casa quando é verdadeira. "Se vomitou."
                    v_casou := (v_bool = 1);

                ELSE
                    -- EXCEÇÃO 4b — regra numérica contra extração sem número.
                    IF v_num IS NULL THEN
                        RAISE_APPLICATION_ERROR(-20014,
                            'DOSE_DO_DIA: regra ' || r.ID_REGRA_PRESCRICAO
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
            -- erro engolido é o pior desfecho possível: o chamador receberia NULL
            -- e poderia interpretar como "sem dose hoje".
            RAISE_APPLICATION_ERROR(-20019,
                'DOSE_DO_DIA: falha inesperada ao calcular a dose — ' || SQLERRM);
    END DOSE_DO_DIA;


    -- =========================================================================
    -- PROCEDIMENTO 2 — ADESAO_POR_DESFECHO
    -- =========================================================================
    -- Tabela de fatos: T_PB_ITEM_PLANO_CUIDADO, com JOIN em T_PB_PRESCRICAO.
    --
    --   categórica 1 — o medicamento (via ID_PRESCRICAO)
    --   categórica 2 — TP_DESFECHO
    --   numérica     — NR_DOSE_APLICADA
    --
    -- O CURSOR FILTRA TP_ORIGEM = 'PRESCRICAO', e o motivo é do enunciado, não
    -- de conveniência. Subtotal e total geral aparecem com as colunas de
    -- agrupamento NULAS — é assim que se distingue uma linha de subtotal de uma
    -- de detalhe. Item de origem PROTOCOLO tem TP_DESFECHO nulo (vacina não tem
    -- desfecho de dose), então sem o filtro uma linha de DETALHE sairia
    -- visualmente idêntica a uma de SUBTOTAL.
    --
    -- O ALGORITMO é quebra de controle em DOIS níveis: o cursor traz as linhas
    -- CRUAS, ordenadas pelas duas categóricas, e todos os totais são acumulados
    -- em variáveis PL/SQL.
    --
    -- DUAS COLUNAS DE MEDIDA: NR_DOSE_APLICADA é NULA quando o desfecho é
    -- SEM_DOSE ou ACIONAR_CLINICA. Somar só a dose esconderia justamente os dias
    -- em que o remédio NÃO foi dado, que são os que interessam clinicamente. Por
    -- isso a saída traz quantidade de ocorrências ao lado da soma.
    --
    -- Exceções tratadas: 4 (três nomeadas + OTHERS).

    -- Privado: uma linha da listagem. NVL(...,' ') põe o subtotal e o total geral
    -- NA MESMA COLUNA das linhas de detalhe, com o valor de agrupamento ausente.
    PROCEDURE linha (
        p_medicamento IN VARCHAR2,
        p_desfecho    IN VARCHAR2,
        p_qtd         IN NUMBER,
        p_soma        IN NUMBER,
        p_marca       IN VARCHAR2 DEFAULT NULL
    ) IS
    BEGIN
        DBMS_OUTPUT.PUT_LINE(
            RPAD(NVL(p_medicamento, ' '), C_W_MED) || ' | ' ||
            RPAD(NVL(p_desfecho,    ' '), C_W_DES) || ' | ' ||
            LPAD(TO_CHAR(p_qtd), 5) || ' | ' ||
            LPAD(TO_CHAR(NVL(p_soma, 0), 'FM99990.000'), 12) ||
            NVL(' ' || p_marca, ''));
    END linha;

    -- Privado: cabeçalho da listagem.
    PROCEDURE cabecalho IS
    BEGIN
        DBMS_OUTPUT.PUT_LINE(RPAD('MEDICAMENTO', C_W_MED) || ' | ' ||
                             RPAD('DESFECHO',    C_W_DES) || ' | ' ||
                             LPAD('QTD', 5) || ' | ' || LPAD('SOMA DOSE', 12));
        DBMS_OUTPUT.PUT_LINE(RPAD('-', C_W_MED, '-') || '-+-' ||
                             RPAD('-', C_W_DES, '-') || '-+-' ||
                             RPAD('-', 5, '-') || '-+-' || RPAD('-', 12, '-'));
    END cabecalho;

    PROCEDURE ADESAO_POR_DESFECHO (
        p_data_inicio IN DATE DEFAULT NULL,
        p_data_fim    IN DATE DEFAULT NULL
    ) IS
        EX_PERIODO_INVALIDO  EXCEPTION;
        EX_SEM_DADOS         EXCEPTION;
        EX_DETALHE_INSUF     EXCEPTION;
        PRAGMA EXCEPTION_INIT(EX_PERIODO_INVALIDO, -20031);
        PRAGMA EXCEPTION_INIT(EX_SEM_DADOS,        -20032);
        PRAGMA EXCEPTION_INIT(EX_DETALHE_INSUF,    -20033);

        C_MIN_DETALHE CONSTANT PLS_INTEGER := 5;

        -- Acumuladores: o "somatório manual" em três níveis, todos somados linha
        -- a linha no laço abaixo.
        v_soma_det     NUMBER := 0;   -- combinação (medicamento, desfecho)
        v_qtd_det      PLS_INTEGER := 0;
        v_soma_grupo   NUMBER := 0;   -- subtotal do medicamento corrente
        v_qtd_grupo    PLS_INTEGER := 0;
        v_soma_geral   NUMBER := 0;   -- total geral
        v_qtd_geral    PLS_INTEGER := 0;

        v_medicamento_ant  T_PB_PRESCRICAO.NM_MEDICAMENTO%TYPE := NULL;
        v_desfecho_ant     T_PB_ITEM_PLANO_CUIDADO.TP_DESFECHO%TYPE := NULL;
        v_primeira         BOOLEAN := TRUE;
        v_linhas_detalhe   PLS_INTEGER := 0;

    BEGIN
        -- EXCEÇÃO 1 — período invertido.
        -- Sem esta checagem o cursor volta vazio e a saída seria "sem dados no
        -- período", mandando procurar o problema na carga em vez do parâmetro.
        IF p_data_inicio IS NOT NULL AND p_data_fim IS NOT NULL
           AND p_data_inicio > p_data_fim THEN
            RAISE_APPLICATION_ERROR(-20031,
                'ADESAO_POR_DESFECHO: data inicial ('
                || TO_CHAR(p_data_inicio, 'DD/MM/YYYY') || ') é posterior à final ('
                || TO_CHAR(p_data_fim, 'DD/MM/YYYY') || ').');
        END IF;

        cabecalho;

        -- Linhas CRUAS. Sem GROUP BY, sem SUM, sem COUNT: só a ordenação, que é o
        -- que a quebra de controle precisa para funcionar.
        FOR r IN (SELECT p.NM_MEDICAMENTO        AS medicamento,
                         item.TP_DESFECHO        AS desfecho,
                         item.NR_DOSE_APLICADA   AS dose
                    FROM T_PB_ITEM_PLANO_CUIDADO item
                    JOIN T_PB_PRESCRICAO p
                      ON p.ID_PRESCRICAO = item.ID_PRESCRICAO
                   -- o filtro do cabeçalho: só item de tratamento tem desfecho
                   WHERE item.TP_ORIGEM = 'PRESCRICAO'
                     AND (p_data_inicio IS NULL OR item.DT_DATA_ALVO >= p_data_inicio)
                     AND (p_data_fim    IS NULL OR item.DT_DATA_ALVO <= p_data_fim)
                   ORDER BY p.NM_MEDICAMENTO, item.TP_DESFECHO)
        LOOP
            -- QUEBRA DE 2º NÍVEL — mudou a combinação: fecha a linha de detalhe.
            IF NOT v_primeira
               AND (r.medicamento <> v_medicamento_ant OR r.desfecho <> v_desfecho_ant) THEN
                linha(v_medicamento_ant, v_desfecho_ant, v_qtd_det, v_soma_det);
                v_linhas_detalhe := v_linhas_detalhe + 1;
                v_soma_det := 0;
                v_qtd_det  := 0;
            END IF;

            -- QUEBRA DE 1º NÍVEL — mudou o medicamento: fecha o subtotal.
            -- Vem depois da anterior de propósito: o subtotal precisa incluir a
            -- combinação que acabou de ser emitida.
            IF NOT v_primeira AND r.medicamento <> v_medicamento_ant THEN
                linha(v_medicamento_ant, NULL, v_qtd_grupo, v_soma_grupo, '<< subtotal');
                v_soma_grupo := 0;
                v_qtd_grupo  := 0;
            END IF;

            -- SOMATÓRIO MANUAL, linha a linha, nos três níveis.
            -- NVL porque NR_DOSE_APLICADA é nula em SEM_DOSE e ACIONAR_CLINICA;
            -- a ocorrência conta mesmo quando não houve dose.
            v_soma_det   := v_soma_det   + NVL(r.dose, 0);
            v_qtd_det    := v_qtd_det    + 1;
            v_soma_grupo := v_soma_grupo + NVL(r.dose, 0);
            v_qtd_grupo  := v_qtd_grupo  + 1;
            v_soma_geral := v_soma_geral + NVL(r.dose, 0);
            v_qtd_geral  := v_qtd_geral  + 1;

            v_medicamento_ant := r.medicamento;
            v_desfecho_ant    := r.desfecho;
            v_primeira        := FALSE;
        END LOOP;

        -- EXCEÇÃO 2 — nenhum resultado no período.
        IF v_primeira THEN
            RAISE_APPLICATION_ERROR(-20032,
                'ADESAO_POR_DESFECHO: não há item de tratamento'
                || CASE WHEN p_data_inicio IS NOT NULL OR p_data_fim IS NOT NULL
                        THEN ' no período informado' ELSE '' END || '.');
        END IF;

        -- A última combinação e o último grupo não têm quem os feche pela quebra,
        -- porque o laço acabou. Fecham aqui, na mesma ordem.
        linha(v_medicamento_ant, v_desfecho_ant, v_qtd_det, v_soma_det);
        v_linhas_detalhe := v_linhas_detalhe + 1;
        linha(v_medicamento_ant, NULL, v_qtd_grupo, v_soma_grupo, '<< subtotal');

        -- Total geral: as DUAS colunas de agrupamento ausentes.
        DBMS_OUTPUT.PUT_LINE(RPAD('-', C_W_MED, '-') || '-+-' ||
                             RPAD('-', C_W_DES, '-') || '-+-' ||
                             RPAD('-', 5, '-') || '-+-' || RPAD('-', 12, '-'));
        linha(NULL, NULL, v_qtd_geral, v_soma_geral, '<< TOTAL GERAL');

        -- EXCEÇÃO 3 — detalhe insuficiente para o que a disciplina pede: no
        -- mínimo 5 linhas detalhadas. O erro vem DEPOIS da listagem, de propósito,
        -- para o print mostrar as duas coisas.
        IF v_linhas_detalhe < C_MIN_DETALHE THEN
            RAISE_APPLICATION_ERROR(-20033,
                'ADESAO_POR_DESFECHO: a listagem produziu apenas '
                || v_linhas_detalhe || ' linha(s) de detalhe; a disciplina exige no '
                || 'mínimo ' || C_MIN_DETALHE || '. Amplie a carga ou o período.');
        END IF;

    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE BETWEEN -20033 AND -20031 THEN
                RAISE;
            END IF;
            -- EXCEÇÃO 4 — qualquer outra.
            RAISE_APPLICATION_ERROR(-20039,
                'ADESAO_POR_DESFECHO: falha inesperada — ' || SQLERRM);
    END ADESAO_POR_DESFECHO;

END PKG_PB_TRATAMENTO;
/


-- =============================================================================
-- PKG_PB_AUDITORIA — especificação
-- =============================================================================
-- A trigger PB_AUDITA_CONDICAO (bloco 06) não pode morar num pacote — trigger é
-- objeto próprio do banco. O que dá para empacotar é o que ela FAZ: montar o
-- JSON da linha e gravar a auditoria. Ela passa a só decidir a operação e
-- chamar o pacote.
CREATE OR REPLACE PACKAGE PKG_PB_AUDITORIA AS

    -- A condição clínica em JSON, para o lado anterior ou novo da auditoria.
    FUNCTION CONDICAO_JSON (
        p_id          IN T_PB_CONDICAO_CLINICA.ID_CONDICAO_CLINICA%TYPE,
        p_codigo      IN T_PB_CONDICAO_CLINICA.CD_CODIGO%TYPE,
        p_rotulo      IN T_PB_CONDICAO_CLINICA.DS_ROTULO%TYPE,
        p_tipo_valor  IN T_PB_CONDICAO_CLINICA.TP_DADO%TYPE,
        p_fonte       IN T_PB_CONDICAO_CLINICA.TP_FONTE_VALOR%TYPE,
        p_unidade     IN T_PB_CONDICAO_CLINICA.DS_UNIDADE%TYPE,
        p_critica     IN T_PB_CONDICAO_CLINICA.FL_CRITICA%TYPE,
        p_ativo       IN T_PB_CONDICAO_CLINICA.AT_ATIVO%TYPE,
        p_clinica_id  IN T_PB_CONDICAO_CLINICA.ID_CLINICA%TYPE,
        p_autor_id    IN T_PB_CONDICAO_CLINICA.ID_VETERINARIO_AUTOR%TYPE
    ) RETURN CLOB;

    -- Grava uma linha em T_PB_AUDITORIA.
    PROCEDURE REGISTRAR (
        p_tabela       IN VARCHAR2,
        p_operacao     IN VARCHAR2,
        p_id_registro  IN NUMBER,
        p_anterior     IN CLOB,
        p_novo         IN CLOB
    );

END PKG_PB_AUDITORIA;
/


-- =============================================================================
-- PKG_PB_AUDITORIA — corpo
-- =============================================================================
CREATE OR REPLACE PACKAGE BODY PKG_PB_AUDITORIA AS

    FUNCTION CONDICAO_JSON (
        p_id          IN T_PB_CONDICAO_CLINICA.ID_CONDICAO_CLINICA%TYPE,
        p_codigo      IN T_PB_CONDICAO_CLINICA.CD_CODIGO%TYPE,
        p_rotulo      IN T_PB_CONDICAO_CLINICA.DS_ROTULO%TYPE,
        p_tipo_valor  IN T_PB_CONDICAO_CLINICA.TP_DADO%TYPE,
        p_fonte       IN T_PB_CONDICAO_CLINICA.TP_FONTE_VALOR%TYPE,
        p_unidade     IN T_PB_CONDICAO_CLINICA.DS_UNIDADE%TYPE,
        p_critica     IN T_PB_CONDICAO_CLINICA.FL_CRITICA%TYPE,
        p_ativo       IN T_PB_CONDICAO_CLINICA.AT_ATIVO%TYPE,
        p_clinica_id  IN T_PB_CONDICAO_CLINICA.ID_CLINICA%TYPE,
        p_autor_id    IN T_PB_CONDICAO_CLINICA.ID_VETERINARIO_AUTOR%TYPE
    ) RETURN CLOB IS
    BEGIN
        RETURN PKG_PB_JSON.MONTA_JSON(PB_PARES_JSON(
            PB_PAR_JSON('id',        TO_CHAR(p_id),          'NUMERO'),
            PB_PAR_JSON('codigo',    p_codigo,               'TEXTO'),
            PB_PAR_JSON('rotulo',    p_rotulo,               'TEXTO'),
            PB_PAR_JSON('tipoValor', p_tipo_valor,           'TEXTO'),
            PB_PAR_JSON('fonte',     p_fonte,                'TEXTO'),
            PB_PAR_JSON('unidade',   p_unidade,              'TEXTO'),
            -- A coluna que justifica a auditoria existir.
            PB_PAR_JSON('critica',   TO_CHAR(p_critica),     'BOOLEANO'),
            PB_PAR_JSON('ativo',     TO_CHAR(p_ativo),       'BOOLEANO'),
            PB_PAR_JSON('clinicaId', TO_CHAR(p_clinica_id),  'NUMERO'),
            PB_PAR_JSON('autorId',   TO_CHAR(p_autor_id),    'NUMERO')
        ));
    END CONDICAO_JSON;

    PROCEDURE REGISTRAR (
        p_tabela       IN VARCHAR2,
        p_operacao     IN VARCHAR2,
        p_id_registro  IN NUMBER,
        p_anterior     IN CLOB,
        p_novo         IN CLOB
    ) IS
    BEGIN
        -- Operação fora das três seria barrada pelo CK_AUDITORIA_OPERACAO com
        -- ORA-02290, que não diz qual chamador errou. Aqui a mensagem diz.
        IF p_operacao NOT IN ('INSERT', 'UPDATE', 'DELETE') OR p_operacao IS NULL THEN
            RAISE_APPLICATION_ERROR(-20042,
                'PKG_PB_AUDITORIA.REGISTRAR: operação "' || p_operacao
                || '" inválida para ' || p_tabela || '.');
        END IF;

        INSERT INTO T_PB_AUDITORIA (
            NM_USUARIO, TP_OPERACAO, DH_OPERACAO, NM_TABELA, ID_REGISTRO,
            TX_VALORES_ANTERIORES, TX_VALORES_NOVOS
        ) VALUES (
            -- SESSION_USER é o usuário do BANCO. Como as duas APIs conectam com a
            -- mesma conta Oracle, este nome é sempre o mesmo — atende o requisito,
            -- mas não identifica a veterinária. Limitação conhecida (bloco 02).
            NVL(SYS_CONTEXT('USERENV', 'SESSION_USER'), USER),
            p_operacao,
            SYSTIMESTAMP,
            p_tabela,
            p_id_registro,
            p_anterior,
            p_novo
        );
    END REGISTRAR;

END PKG_PB_AUDITORIA;
/
