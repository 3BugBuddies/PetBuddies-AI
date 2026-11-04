package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Auditoria;
import br.com.fiap.petbuddies.domain.enums.pontuacao.StatusEnvioPontuacao;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * O aviso de consulta realizada que o .NET precisa receber para pontuar o
 * responsável. Nasce PENDENTE na mesma transação do fechamento: se o .NET
 * estiver fora do ar, a linha fica aqui até o reenvio agendado conseguir.
 */
@Entity
@Table(name = "T_PB_ENVIO_PONTUACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvioPontuacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ENVIO_PONTUACAO")
    private Long id;

    // UK_ENVIO_PONT_CONSULTA: um envio por consulta.
    @Column(name = "ID_CONSULTA", nullable = false, unique = true)
    private Long consultaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS_ENVIO", nullable = false, length = 20)
    private StatusEnvioPontuacao status = StatusEnvioPontuacao.PENDENTE;

    @Column(name = "NR_TENTATIVAS", nullable = false)
    private Integer tentativas = 0;

    @Column(name = "DS_ULTIMO_ERRO", length = 500)
    private String ultimoErro;

    @Column(name = "DH_ENVIADO_EM")
    private LocalDateTime enviadoEm;

    @Embedded
    @Setter(AccessLevel.NONE)
    private Auditoria auditoria;

    @PrePersist
    private void prePersist() { auditoria = Auditoria.criadaAgora(); }

    @PreUpdate
    private void preUpdate() { auditoria = auditoria.atualizadaAgora(); }
}
