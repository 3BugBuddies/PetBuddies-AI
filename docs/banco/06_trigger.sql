-- =============================================================================
-- 06 — TRIGGER PB_AUDITA_CONDICAO
-- =============================================================================
-- Trigger não pode morar dentro de pacote — é objeto próprio do banco. Por isso
-- ela ficou só com o que é dela: saber qual DML disparou e ler :OLD e :NEW. A
-- montagem do JSON e a gravação da auditoria estão em PKG_PB_AUDITORIA (bloco
-- 05), reaproveitáveis por uma próxima trigger de outra tabela.
-- =============================================================================
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
    -- chamada em três gatilhos separados.
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
        v_anterior := PKG_PB_AUDITORIA.CONDICAO_JSON(
            :OLD.ID_CONDICAO_CLINICA, :OLD.CD_CODIGO, :OLD.DS_ROTULO, :OLD.TP_DADO,
            :OLD.TP_FONTE_VALOR, :OLD.DS_UNIDADE, :OLD.FL_CRITICA, :OLD.AT_ATIVO,
            :OLD.ID_CLINICA, :OLD.ID_VETERINARIO_AUTOR);
    END IF;

    -- Estado NOVO: existe em INSERT e UPDATE, nunca em DELETE.
    IF INSERTING OR UPDATING THEN
        v_novo := PKG_PB_AUDITORIA.CONDICAO_JSON(
            :NEW.ID_CONDICAO_CLINICA, :NEW.CD_CODIGO, :NEW.DS_ROTULO, :NEW.TP_DADO,
            :NEW.TP_FONTE_VALOR, :NEW.DS_UNIDADE, :NEW.FL_CRITICA, :NEW.AT_ATIVO,
            :NEW.ID_CLINICA, :NEW.ID_VETERINARIO_AUTOR);
    END IF;

    PKG_PB_AUDITORIA.REGISTRAR('T_PB_CONDICAO_CLINICA', v_operacao, v_id, v_anterior, v_novo);

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
