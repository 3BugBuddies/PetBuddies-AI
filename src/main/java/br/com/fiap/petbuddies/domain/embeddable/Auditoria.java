package br.com.fiap.petbuddies.domain.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    private LocalDateTime updatedAt;

    public static Auditoria criadaAgora() {
        return new Auditoria(LocalDateTime.now(), null);
    }

    public Auditoria atualizadaAgora() {
        return new Auditoria(createdAt, LocalDateTime.now());
    }
}
