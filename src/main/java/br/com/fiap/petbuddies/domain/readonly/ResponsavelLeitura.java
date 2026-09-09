package br.com.fiap.petbuddies.domain.readonly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Projecao de leitura do tutor, sobre a tabela que o servico .NET escreve.
 *
 * <p>Mesmas regras da {@link AnimalLeitura}: identificador e nome, nada mais,
 * e nenhuma escrita.</p>
 */
@Entity
@Immutable
@Table(name = "T_PB_RESPONSAVEL")
public class ResponsavelLeitura {

    @Id
    @Column(name = "ID_RESPONSAVEL")
    private Long id;

    @Column(name = "NM_NOME_RESPONSAVEL")
    private String nome;

    protected ResponsavelLeitura() {
        // exigido pelo Hibernate
    }

    public Long getId() { return id; }

    public String getNome() { return nome; }
}
