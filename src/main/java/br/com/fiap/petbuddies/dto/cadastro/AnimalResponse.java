package br.com.fiap.petbuddies.dto.cadastro;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;
import br.com.fiap.petbuddies.domain.enums.cadastro.Porte;
import br.com.fiap.petbuddies.domain.enums.cadastro.Sexo;
import lombok.*;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnimalResponse {

    private Long id;
    private String nome;
    private Especie especie;
    private String raca;
    private Porte porte;
    private Sexo sexo;
    private LocalDate dataNascimento;
    private BigDecimal peso;
    private Boolean condicaoCronica;
    private Boolean castrado;
    private String foto;
    private String alergias;
    private String observacoes;
    private Long responsavelId;
    private String responsavelNome;
    private String responsavelTelefone;
    private String responsavelEndereco;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnimalResponse from(AnimalEntity entity) {
        AnimalResponse dto = new AnimalResponse();
        dto.id = entity.getId();
        dto.nome = entity.getNome();
        dto.especie = entity.getEspecie();
        dto.raca = entity.getRaca();
        dto.porte = entity.getPorte();
        dto.sexo = entity.getSexo();
        dto.dataNascimento = entity.getDataNascimento();
        dto.peso = entity.getPeso();
        dto.condicaoCronica = entity.isCondicaoCronica();
        dto.castrado = entity.isCastrado();
        dto.foto = entity.getFoto();
        dto.alergias = entity.getAlergias();
        dto.observacoes = entity.getObservacoes();
        dto.responsavelId = entity.getResponsavel() == null ? null : entity.getResponsavel().getId();
        // Com open-in-view=false, proxy LAZY não inicializado lançaria LazyInitializationException; a leitura precisa do JOIN FETCH.
        ResponsavelEntity responsavel = entity.getResponsavel();
        if (responsavel != null && Hibernate.isInitialized(responsavel)) {
            dto.responsavelNome = responsavel.getNome();
            dto.responsavelTelefone = responsavel.getContato() == null ? null : responsavel.getContato().getTelefone();
            dto.responsavelEndereco = responsavel.getEndereco();
        }
        dto.createdAt = entity.getAuditoria().getCreatedAt();
        dto.updatedAt = entity.getAuditoria().getUpdatedAt();
        return dto;
    }
}
