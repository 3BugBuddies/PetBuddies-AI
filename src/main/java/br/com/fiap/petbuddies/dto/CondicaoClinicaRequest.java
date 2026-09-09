package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.TipoDado;
import br.com.fiap.petbuddies.domain.enums.TipoFonteValor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CondicaoClinicaRequest {

    @NotBlank(message = "Código é obrigatório.")
    @Size(max = 60, message = "Código deve ter no máximo 60 caracteres.")
    private String codigo;

    @NotBlank(message = "Rótulo é obrigatório.")
    @Size(max = 255, message = "Rótulo deve ter no máximo 255 caracteres.")
    private String rotulo;

    @NotNull(message = "Tipo do dado é obrigatório.")
    private TipoDado tipoDado;

    /** Ausente, assume RELATO — o default de TP_FONTE_VALOR no DDL. */
    private TipoFonteValor fonteValor;

    @Size(max = 20, message = "Unidade deve ter no máximo 20 caracteres.")
    private String unidade;

    private Boolean critica;

    private Boolean ativo;

    @NotNull(message = "Clínica é obrigatória.")
    private Long clinicaId;

    @NotNull(message = "Veterinário autor é obrigatório.")
    private Long veterinarioAutorId;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getRotulo() { return rotulo; }
    public void setRotulo(String rotulo) { this.rotulo = rotulo; }

    public TipoDado getTipoDado() { return tipoDado; }
    public void setTipoDado(TipoDado tipoDado) { this.tipoDado = tipoDado; }

    public TipoFonteValor getFonteValor() { return fonteValor; }
    public void setFonteValor(TipoFonteValor fonteValor) { this.fonteValor = fonteValor; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public Boolean getCritica() { return critica; }
    public void setCritica(Boolean critica) { this.critica = critica; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }

    public Long getVeterinarioAutorId() { return veterinarioAutorId; }
    public void setVeterinarioAutorId(Long veterinarioAutorId) { this.veterinarioAutorId = veterinarioAutorId; }
}
