-- Sinal de alarme no check-in suspende o plano de tratamento: a veterinária
-- avalia antes de o responsável seguir dando o remédio (decisão D1, 23/10/2026).
ALTER TABLE T_PB_PLANO_CUIDADO DROP CONSTRAINT CK_PLANO_STATUS;
ALTER TABLE T_PB_PLANO_CUIDADO ADD CONSTRAINT CK_PLANO_STATUS
    CHECK (ST_STATUS_PLANO IN ('ATIVO','CONCLUIDO','CANCELADO','SUSPENSO'));
