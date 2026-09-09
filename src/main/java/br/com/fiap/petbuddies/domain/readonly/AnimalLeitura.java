package br.com.fiap.petbuddies.domain.readonly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Projecao de leitura do pet, sobre a tabela que o servico .NET escreve.
 *
 * <p>Sem setter, sem cascade e sem relacao JPA: esta classe nunca escreve. A
 * {@link Immutable} torna isso garantia do Hibernate, e nao convencao — uma
 * tentativa de escrita e erro de desenho, nao de configuracao.</p>
 *
 * <p>Mapeia SO as colunas que algum consumidor le. Mapear a tabela inteira
 * transformaria cada coluna do .NET em acoplamento novo, e as colunas de la
 * mudam — o {@code N1} acabou de prova-lo.</p>
 */
@Entity
@Immutable
@Table(name = "T_PB_ANIMAL")
public class AnimalLeitura {

    @Id
    @Column(name = "ID_ANIMAL")
    private Long id;

    @Column(name = "NM_NOME_ANIMAL")
    private String nome;

    @Column(name = "ID_RESPONSAVEL")
    private Long responsavelId;

    protected AnimalLeitura() {
        // exigido pelo Hibernate
    }

    public Long getId() { return id; }

    public String getNome() { return nome; }

    public Long getResponsavelId() { return responsavelId; }
}
