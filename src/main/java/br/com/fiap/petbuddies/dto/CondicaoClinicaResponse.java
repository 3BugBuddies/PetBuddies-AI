package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.enums.TipoDado;
import br.com.fiap.petbuddies.domain.enums.TipoFonteValor;

import java.time.LocalDateTime;

public class CondicaoClinicaResponse {

    private Long id;
    private String codigo;
    private String rotulo;
    private TipoDado tipoDado;
    private TipoFonteValor fonteValor;
    private String unidade;
    private Boolean critica;
    private Boolean ativo;
    private Long clinicaId;
    private Long veterinarioAutorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CondicaoClinicaResponse from(CondicaoClinicaEntity entity) {
        CondicaoClinicaResponse dto = new CondicaoClinicaResponse();
        dto.id = entity.getId();
        dto.codigo = entity.getCodigo();
        dto.rotulo = entity.getRotulo();
        dto.tipoDado = entity.getTipoDado();
        dto.fonteValor = entity.getFonteValor();
        dto.unidade = entity.getUnidade();
        dto.critica = entity.isCritica();
        dto.ativo = entity.isAtivo();
        // Só os ids: com open-in-view=false, ler outro campo do proxy LAZY aqui lança LazyInitializationException.
        dto.clinicaId = entity.getClinica() == null ? null : entity.getClinica().getId();
        dto.veterinarioAutorId = entity.getAutor() == null ? null : entity.getAutor().getId();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getRotulo() { return rotulo; }
    public TipoDado getTipoDado() { return tipoDado; }
    public TipoFonteValor getFonteValor() { return fonteValor; }
    public String getUnidade() { return unidade; }
    public Boolean getCritica() { return critica; }
    public Boolean getAtivo() { return ativo; }
    public Long getClinicaId() { return clinicaId; }
    public Long getVeterinarioAutorId() { return veterinarioAutorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
