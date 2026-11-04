
--   Procedimento 1 ? PB_PLANO_JSON            (JOIN - JSON, via PB_MONTA_JSON)
--   Procedimento 2 ? PB_ADESAO_POR_DESFECHO   (fatos, subtotal e total manuais)

-- =============================================================================
-- PROCEDIMENTO 1 — PB_PLANO_JSON
-- =============================================================================
-- JOIN de três tabelas, saída em JSON string, conversão feita pela Função 1.
--
--   T_PB_ANIMAL - T_PB_PLANO_CUIDADO -  T_PB_ITEM_PLANO_CUIDADO
--
-- É o plano de cuidado do pet: quem ele é, qual plano está ativo, e cada item
-- do checklist com data-alvo e situação. As três têm cinco ou mais registros na
-- carga do bloco 03.
--
-- FORMA DO DOCUMENTO, e por que ela importa além desta sprint: os eventos vêm
-- EMBUTIDOS num array dentro do plano, não em linhas repetidas com os dados do
-- animal duplicados. Isso é o que a Sprint 4 vai importar no MongoDB — um
-- documento por plano, com a lista dentro, que é a modelagem natural de um
-- banco de documentos. Uma saída achatada exigiria reagrupar tudo depois.
--
-- É para isso que a Função 1 ganhou o tipo JSON: o array é montado por chamadas
-- a ela e devolvido cru para a chamada de fora, sem virar string escapada.
--
-- Exceções tratadas: 4 (três nomeadas + OTHERS).
-- =============================================================================
CREATE OR REPLACE PROCEDURE PB_PLANO_JSON (
    p_id_animal IN NUMBER
) IS
    EX_ANIMAL_INEXISTENTE  EXCEPTION;
    EX_SEM_PLANO           EXCEPTION;
    EX_PLANO_SEM_EVENTOS   EXCEPTION;
    PRAGMA EXCEPTION_INIT(EX_ANIMAL_INEXISTENTE, -20021);
    PRAGMA EXCEPTION_INIT(EX_SEM_PLANO,          -20022);
    PRAGMA EXCEPTION_INIT(EX_PLANO_SEM_EVENTOS,  -20023);

    v_nome        T_PB_ANIMAL.NM_NOME_ANIMAL%TYPE;
    v_especie     T_PB_ANIMAL.ES_ESPECIE%TYPE;
    v_raca        T_PB_ANIMAL.RC_RACA%TYPE;
    v_porte       T_PB_ANIMAL.PT_PORTE%TYPE;
    v_nascimento  T_PB_ANIMAL.DT_DATA_NASCIMENTO%TYPE;
    v_peso        T_PB_ANIMAL.NR_PESO%TYPE;

    v_planos      PLS_INTEGER := 0;
    v_eventos     PLS_INTEGER;

    v_doc         CLOB;
    v_evento      CLOB;
    v_array       CLOB;

    -- Imprime um CLOB em pedaços. DBMS_OUTPUT.PUT_LINE não aceita mais de 32767
    -- caracteres de uma vez, e um plano com muitos eventos passa disso.
    
    PROCEDURE imprimir(p_clob IN CLOB) IS
        C_PEDACO CONSTANT PLS_INTEGER := 8000;
        v_pos    PLS_INTEGER := 1;
        v_total  PLS_INTEGER := DBMS_LOB.GETLENGTH(p_clob);
    BEGIN
        WHILE v_pos <= v_total LOOP
            DBMS_OUTPUT.PUT_LINE(DBMS_LOB.SUBSTR(p_clob, C_PEDACO, v_pos));
            v_pos := v_pos + C_PEDACO;
        END LOOP;
    END imprimir;

BEGIN
    -- EXCEÇÃO 1 — animal inexistente.
    BEGIN
        SELECT NM_NOME_ANIMAL, ES_ESPECIE, RC_RACA, PT_PORTE,
               DT_DATA_NASCIMENTO, NR_PESO
          INTO v_nome, v_especie, v_raca, v_porte, v_nascimento, v_peso
          FROM T_PB_ANIMAL
         WHERE ID_ANIMAL = p_id_animal;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20021,
                'PB_PLANO_JSON: animal ' || p_id_animal || ' não existe.');
    END;

    FOR pl IN (SELECT ID_PLANO_CUIDADO, ID_PROTOCOLO, ID_CONSULTA,
                      TP_CATEGORIA_PLANO, ST_STATUS_PLANO, CA_CREATED_AT
                 FROM T_PB_PLANO_CUIDADO
                WHERE ID_ANIMAL = p_id_animal
                ORDER BY ID_PLANO_CUIDADO)
    LOOP
        v_planos  := v_planos + 1;
        v_eventos := 0;
        v_array   := NULL;
        DBMS_LOB.CREATETEMPORARY(v_array, TRUE);
        DBMS_LOB.WRITEAPPEND(v_array, 1, '[');

        -- Um objeto JSON por item do checklist, montado pela Função 1 e
        -- concatenado dentro do array.
        FOR ev IN (SELECT ID_ITEM_PLANO_CUIDADO, TP_ORIGEM, TP_TIPO_CUIDADO, NM_NOME,
                          DT_DATA_ALVO, ST_STATUS_ITEM, ID_PRESCRICAO,
                          ID_PROCEDIMENTO, DT_EXECUTADO_EM
                     FROM T_PB_ITEM_PLANO_CUIDADO
                    WHERE ID_PLANO_CUIDADO = pl.ID_PLANO_CUIDADO
                    ORDER BY DT_DATA_ALVO, ID_ITEM_PLANO_CUIDADO)
        LOOP
            v_eventos := v_eventos + 1;
            IF v_eventos > 1 THEN
                DBMS_LOB.WRITEAPPEND(v_array, 1, ',');
            END IF;

            v_evento := PB_MONTA_JSON(PB_PARES_JSON(
                PB_PAR_JSON('id',          TO_CHAR(ev.ID_ITEM_PLANO_CUIDADO),                'NUMERO'),
                PB_PAR_JSON('origem',      ev.TP_ORIGEM,                               'TEXTO'),
                PB_PAR_JSON('tipo',        ev.TP_TIPO_CUIDADO,                                 'TEXTO'),
                PB_PAR_JSON('nome',        ev.NM_NOME,                                 'TEXTO'),
                PB_PAR_JSON('dataAlvo',    TO_CHAR(ev.DT_DATA_ALVO, 'YYYY-MM-DD'),     'DATA'),
                PB_PAR_JSON('status',      ev.ST_STATUS_ITEM,                               'TEXTO'),
                -- "quem faz o item" é DERIVADO de TP_ORIGEM, não é coluna:
                -- PRESCRICAO é em casa, PROTOCOLO é na clínica.
                PB_PAR_JSON('feitoEmCasa', CASE ev.TP_ORIGEM WHEN 'PRESCRICAO' THEN '1'
                                                             ELSE '0' END,             'BOOLEANO'),
                PB_PAR_JSON('prescricaoId',  TO_CHAR(ev.ID_PRESCRICAO),                'NUMERO'),
                PB_PAR_JSON('procedimentoId',TO_CHAR(ev.ID_PROCEDIMENTO),              'NUMERO'),
                PB_PAR_JSON('executadoEm', TO_CHAR(ev.DT_EXECUTADO_EM,
                                                   'YYYY-MM-DD"T"HH24:MI:SS'),         'DATA')
            ));
            DBMS_LOB.APPEND(v_array, v_evento);
        END LOOP;

        DBMS_LOB.WRITEAPPEND(v_array, 1, ']');

        -- EXCEÇÃO 2 — plano sem nenhum evento.
        -- Não é caso de borda inofensivo: um plano vazio quer dizer que a
        -- instanciação falhou no meio, e o tutor abriria o app com um checklist
        -- em branco sem nada indicando erro.
        IF v_eventos = 0 THEN
            RAISE_APPLICATION_ERROR(-20023,
                'PB_PLANO_JSON: plano ' || pl.ID_PLANO_CUIDADO
                || ' do animal ' || p_id_animal || ' não tem nenhum evento. '
                || 'A instanciação do plano não terminou.');
        END IF;

        -- O documento: animal e plano no topo, eventos embutidos.
        v_doc := PB_MONTA_JSON(PB_PARES_JSON(
            PB_PAR_JSON('planoId',       TO_CHAR(pl.ID_PLANO_CUIDADO),             'NUMERO'),
            PB_PAR_JSON('categoria',     pl.TP_CATEGORIA_PLANO,                          'TEXTO'),
            PB_PAR_JSON('status',        pl.ST_STATUS_PLANO,                             'TEXTO'),
            PB_PAR_JSON('protocoloId',   TO_CHAR(pl.ID_PROTOCOLO),                 'NUMERO'),
            PB_PAR_JSON('consultaId',    TO_CHAR(pl.ID_CONSULTA),                  'NUMERO'),
            PB_PAR_JSON('criadoEm',      TO_CHAR(pl.CA_CREATED_AT,
                                                 'YYYY-MM-DD"T"HH24:MI:SS'),       'DATA'),
            PB_PAR_JSON('animalId',      TO_CHAR(p_id_animal),                     'NUMERO'),
            PB_PAR_JSON('animalNome',    v_nome,                                   'TEXTO'),
            PB_PAR_JSON('especie',       v_especie,                                'TEXTO'),
            PB_PAR_JSON('raca',          v_raca,                                   'TEXTO'),
            PB_PAR_JSON('porte',         v_porte,                                  'TEXTO'),
            PB_PAR_JSON('nascimento',    TO_CHAR(v_nascimento, 'YYYY-MM-DD'),      'DATA'),
            PB_PAR_JSON('pesoKg',        TO_CHAR(v_peso),                          'NUMERO'),
            PB_PAR_JSON('totalEventos',  TO_CHAR(v_eventos),                       'NUMERO'),
            -- O array entra CRU. É este par que justifica o tipo JSON existir.
            PB_PAR_JSON('eventos',       v_array,                                  'JSON')
        ));

        imprimir(v_doc);
        DBMS_LOB.FREETEMPORARY(v_array);
    END LOOP;

    -- EXCEÇÃO 3 — animal existe mas não tem plano nenhum.
    -- Separada da primeira de propósito: "o id está errado" e "o motor nunca
    -- criou plano para este pet" são problemas diferentes, e a mensagem que
    -- confunde os dois faz procurar no lugar errado.
    IF v_planos = 0 THEN
        RAISE_APPLICATION_ERROR(-20022,
            'PB_PLANO_JSON: o animal ' || p_id_animal || ' (' || v_nome
            || ') existe, mas não tem plano de cuidado.');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE BETWEEN -20023 AND -20021 THEN
            RAISE;
        END IF;
        -- EXCEÇÃO 4 — inclui os erros que vêm de dentro da Função 1
        -- (-20001 a -20005 e -20009). Repassar com o contexto de quem chamou
        -- evita a caça ao tesouro entre procedure e função.
        RAISE_APPLICATION_ERROR(-20029,
            'PB_PLANO_JSON: falha ao montar o plano do animal ' || p_id_animal
            || ' — ' || SQLERRM);
END PB_PLANO_JSON;
/

-- =============================================================================
-- PROCEDIMENTO 2 — PB_ADESAO_POR_DESFECHO
-- =============================================================================
-- Tabela de fatos: T_PB_ITEM_PLANO_CUIDADO.
--
--   categórica 1 ? o medicamento (via ID_PRESCRICAO)
--   categórica 2 ? TP_DESFECHO
--   numérica     ? NR_DOSE_APLICADA
--
-- O CURSOR FILTRA TP_ORIGEM = 'PRESCRICAO', e o motivo é do enunciado, não de
-- conveniência. O PDF exige que subtotal e total geral apareçam com as colunas
-- de agrupamento NULAS — é assim que se distingue uma linha de subtotal de uma
-- de detalhe. Item de origem PROTOCOLO tem TP_DESFECHO nulo (vacina não tem
-- desfecho de dose), então sem o filtro uma linha de DETALHE sairia visualmente
-- idêntica a uma de SUBTOTAL, e a saída ficaria ambígua exatamente no critério
-- que a questão avalia.
--
-- O filtro também é honesto no domínio: o relatório é de adesão AO TRATAMENTO, e
-- vacina de protocolo não tem dose para somar.
--
-- O ALGORITMO é quebra de controle em DOIS níveis: o cursor traz as linhas
-- CRUAS, ordenadas pelas duas categóricas, e todos os totais são acumulados em
-- variáveis PL/SQL.
--
-- DUAS COLUNAS DE MEDIDA, e o motivo: NR_DOSE_APLICADA é NULA quando o desfecho
-- é SEM_DOSE ou ACIONAR_CLINICA — a constraint CK_RESULTADO_DOSE garante isso.
-- Somar só a dose esconderia justamente os dias em que o remédio NÃO foi dado,
-- que são os que interessam clinicamente. Por isso a saída traz quantidade de
-- ocorrências ao lado da soma.
--
-- Exceções tratadas: 4 (três nomeadas + OTHERS). 

CREATE OR REPLACE PROCEDURE PB_ADESAO_POR_DESFECHO (
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

    -- Acumuladores. É aqui que mora o "somatório manual" 
    -- três níveis, todos somados linha a linha no laço abaixo.
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

    -- Larguras da saída, num lugar só.
    C_W_MED  CONSTANT PLS_INTEGER := 24;
    C_W_DES  CONSTANT PLS_INTEGER := 18;

    PROCEDURE linha (
        p_medicamento IN VARCHAR2,
        p_desfecho    IN VARCHAR2,
        p_qtd         IN NUMBER,
        p_soma        IN NUMBER,
        p_marca       IN VARCHAR2 DEFAULT NULL
    ) IS
    BEGIN
        -- NVL(...,' ') é o que põe o subtotal e o total geral NA MESMA COLUNA
        -- das linhas de detalhe, com o valor de agrupamento ausente
        DBMS_OUTPUT.PUT_LINE(
            RPAD(NVL(p_medicamento, ' '), C_W_MED) || ' | ' ||
            RPAD(NVL(p_desfecho,    ' '), C_W_DES) || ' | ' ||
            LPAD(TO_CHAR(p_qtd), 5) || ' | ' ||
            LPAD(TO_CHAR(NVL(p_soma, 0), 'FM99990.000'), 12) ||
            NVL(' ' || p_marca, ''));
    END linha;

    PROCEDURE cabecalho IS
    BEGIN
        DBMS_OUTPUT.PUT_LINE(RPAD('MEDICAMENTO', C_W_MED) || ' | ' ||
                             RPAD('DESFECHO',    C_W_DES) || ' | ' ||
                             LPAD('QTD', 5) || ' | ' || LPAD('SOMA DOSE', 12));
        DBMS_OUTPUT.PUT_LINE(RPAD('-', C_W_MED, '-') || '-+-' ||
                             RPAD('-', C_W_DES, '-') || '-+-' ||
                             RPAD('-', 5, '-') || '-+-' || RPAD('-', 12, '-'));
    END cabecalho;

BEGIN
    -- EXCEÇÃO 1 — período invertido.
    -- Sem esta checagem o cursor volta vazio e a saída seria "sem dados no
    -- período", mandando procurar o problema na carga em vez do parâmetro.
    IF p_data_inicio IS NOT NULL AND p_data_fim IS NOT NULL
       AND p_data_inicio > p_data_fim THEN
        RAISE_APPLICATION_ERROR(-20031,
            'PB_ADESAO_POR_DESFECHO: data inicial ('
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
            'PB_ADESAO_POR_DESFECHO: não há item de tratamento'
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

    -- EXCEÇÃO 3 — detalhe insuficiente para o que a disciplina pede.
    -- O PDF exige no mínimo 5 linhas detalhadas "para que haja dados suficientes
    -- para os cálculos de subtotal e total geral". Validar aqui é o que impede
    -- entregar um print bonito de uma saída que não cumpre o requisito — o erro
    -- vem DEPOIS da listagem, de propósito, para o print mostrar as duas coisas.
    IF v_linhas_detalhe < C_MIN_DETALHE THEN
        RAISE_APPLICATION_ERROR(-20033,
            'PB_ADESAO_POR_DESFECHO: a listagem produziu apenas '
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
            'PB_ADESAO_POR_DESFECHO: falha inesperada — ' || SQLERRM);
END PB_ADESAO_POR_DESFECHO;
/