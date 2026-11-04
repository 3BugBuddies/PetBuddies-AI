CREATE OR REPLACE TRIGGER PB_AUDITA_CONDICAO
    AFTER INSERT OR UPDATE OR DELETE ON T_PB_CONDICAO_CLINICA
    FOR EACH ROW
DECLARE
    v_operacao   VARCHAR2(10);
    v_anterior   CLOB;
    v_novo       CLOB;
    v_id         NUMBER;
BEGIN
    -- INSERTING/UPDATING/DELETING são os predicados que dizem qual DML disparou
    -- esta execução. Uma trigger única para as três operações evita repetir a
    -- montagem dos pares em três gatilhos separados.
    IF INSERTING THEN
        v_operacao := 'INSERT';
    ELSIF UPDATING THEN
        v_operacao := 'UPDATE';
    ELSE
        v_operacao := 'DELETE';
    END IF;

    -- Em DELETE não existe :NEW; o id tem de vir de :OLD.
    v_id := CASE WHEN DELETING THEN :OLD.ID_CONDICAO_CLINICA
                 ELSE :NEW.ID_CONDICAO_CLINICA END;

    -- Estado ANTERIOR: existe em UPDATE e DELETE, nunca em INSERT.
    IF UPDATING OR DELETING THEN
        v_anterior := PB_MONTA_JSON(PB_PARES_JSON(
            PB_PAR_JSON('id',           TO_CHAR(:OLD.ID_CONDICAO_CLINICA),   'NUMERO'),
            PB_PAR_JSON('codigo',       :OLD.CD_CODIGO,                      'TEXTO'),
            PB_PAR_JSON('rotulo',       :OLD.DS_ROTULO,                      'TEXTO'),
            PB_PAR_JSON('tipoValor',    :OLD.TP_DADO,                       'TEXTO'),
            PB_PAR_JSON('fonte',        :OLD.TP_FONTE_VALOR,                       'TEXTO'),
            PB_PAR_JSON('unidade',      :OLD.DS_UNIDADE,                     'TEXTO'),
            -- A coluna que justifica esta trigger existir.
            PB_PAR_JSON('critica',      TO_CHAR(:OLD.FL_CRITICA),            'BOOLEANO'),
            PB_PAR_JSON('ativo',        TO_CHAR(:OLD.AT_ATIVO),              'BOOLEANO'),
            PB_PAR_JSON('clinicaId',    TO_CHAR(:OLD.ID_CLINICA),            'NUMERO'),
            PB_PAR_JSON('autorId',      TO_CHAR(:OLD.ID_VETERINARIO_AUTOR),  'NUMERO')
        ));
    END IF;

    -- Estado NOVO: existe em INSERT e UPDATE, nunca em DELETE.
    IF INSERTING OR UPDATING THEN
        v_novo := PB_MONTA_JSON(PB_PARES_JSON(
            PB_PAR_JSON('id',           TO_CHAR(:NEW.ID_CONDICAO_CLINICA),   'NUMERO'),
            PB_PAR_JSON('codigo',       :NEW.CD_CODIGO,                      'TEXTO'),
            PB_PAR_JSON('rotulo',       :NEW.DS_ROTULO,                      'TEXTO'),
            PB_PAR_JSON('tipoValor',    :NEW.TP_DADO,                       'TEXTO'),
            PB_PAR_JSON('fonte',        :NEW.TP_FONTE_VALOR,                       'TEXTO'),
            PB_PAR_JSON('unidade',      :NEW.DS_UNIDADE,                     'TEXTO'),
            PB_PAR_JSON('critica',      TO_CHAR(:NEW.FL_CRITICA),            'BOOLEANO'),
            PB_PAR_JSON('ativo',        TO_CHAR(:NEW.AT_ATIVO),              'BOOLEANO'),
            PB_PAR_JSON('clinicaId',    TO_CHAR(:NEW.ID_CLINICA),            'NUMERO'),
            PB_PAR_JSON('autorId',      TO_CHAR(:NEW.ID_VETERINARIO_AUTOR),  'NUMERO')
        ));
    END IF;

    INSERT INTO T_PB_AUDITORIA (
        NM_USUARIO, TP_OPERACAO, DH_OPERACAO, NM_TABELA, ID_REGISTRO,
        TX_VALORES_ANTERIORES, TX_VALORES_NOVOS
    ) VALUES (
        -- SESSION_USER é o usuário do BANCO. Como as duas APIs conectam com a
        -- mesma conta Oracle, este nome é sempre o mesmo — atende o requisito do
        -- PDF, mas não identifica a veterinária. Está registrado como limitação
        -- conhecida no bloco 02.
        NVL(SYS_CONTEXT('USERENV', 'SESSION_USER'), USER),
        v_operacao,
        SYSTIMESTAMP,
        'T_PB_CONDICAO_CLINICA',
        v_id,
        v_anterior,
        v_novo
    );

EXCEPTION
    WHEN OTHERS THEN
        -- A auditoria NÃO engole o próprio erro. Se ela falhar em silêncio, o
        -- UPDATE passa e ninguém fica sabendo — que é exatamente o cenário que
        -- esta trigger existe para impedir. Falhar aqui aborta a DML, e é o
        -- comportamento correto: sem registro, sem mudança.
        RAISE_APPLICATION_ERROR(-20041,
            'PB_AUDITA_CONDICAO: falha ao auditar ' || v_operacao
            || ' na condição ' || v_id || ' — ' || SQLERRM);
END PB_AUDITA_CONDICAO;
/