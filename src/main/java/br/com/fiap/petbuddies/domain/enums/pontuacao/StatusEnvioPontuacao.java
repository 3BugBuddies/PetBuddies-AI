package br.com.fiap.petbuddies.domain.enums.pontuacao;

// CK_ENVIO_PONT_STATUS
public enum StatusEnvioPontuacao {
    /** Ainda não chegou ao .NET: o reenvio agendado tenta de novo. */
    PENDENTE,
    /** O .NET lançou os pontos (ou já tinha lançado esta consulta). */
    ENVIADO,
    /** O .NET recusou os dados (4xx): reenviar o mesmo corpo não adianta. */
    RECUSADO
}
