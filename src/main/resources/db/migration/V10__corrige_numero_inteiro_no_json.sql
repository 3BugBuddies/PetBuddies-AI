-- =============================================================================
-- V10 — número inteiro no JSON sem o ponto sobrando
--
-- GERADO por docs/banco/gerar_migration.py a partir do corpo do PKG_PB_JSON em
-- docs/banco/05_pacotes.sql. Não edite aqui: edite lá e gere de novo.
--
-- A Função 1 (MONTA_JSON) formatava número com TO_CHAR(..., 'FM...0.999'), que
-- deixa o ponto decimal em número inteiro: "planoId":4. — JSON inválido, que o
-- mongoimport recusa. O RTRIM tira esse ponto; decimal (3.5) não muda.
--
-- Só o corpo do pacote é recompilado: a especificação, os tipos e a trigger da
-- V9 ficam como estão, e quem usa o pacote não precisa recompilar.
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
                -- O RTRIM tira o ponto que o FM deixa sobrando em número inteiro
                -- (4 viraria "4."), que o JSON também não aceita. Correção da V10.
                emitir(RTRIM(TO_CHAR(v_numero, 'FM999999999999999990.999999999999',
                                     'NLS_NUMERIC_CHARACTERS = ''.,'''), '.'));

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
