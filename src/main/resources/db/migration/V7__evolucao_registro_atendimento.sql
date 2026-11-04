-- O veterinário informa no fechamento como o animal evoluiu desde a consulta
-- anterior. Opcional: nulo quando não houve avaliação. Entra na pontuação do
-- responsável, calculada no PetBuddies-API (.NET).
ALTER TABLE T_PB_REGISTRO_ATENDIMENTO ADD TP_EVOLUCAO VARCHAR2(20);

ALTER TABLE T_PB_REGISTRO_ATENDIMENTO ADD CONSTRAINT CK_REGATEND_EVOLUCAO
    CHECK (TP_EVOLUCAO IN ('MELHOROU','ESTAVEL','PIOROU'));
