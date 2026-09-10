package br.com.fiap.petbuddies.web.form;

import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * Formulário do fluxo 1 (instanciar plano de cuidado). Só o que o vet escolhe
 * na tela — espécie e data de nascimento vêm do próprio animal, resolvidos no
 * controller.
 */
public class NovoPlanoForm {

    @NotNull(message = "Categoria é obrigatória.")
    private CategoriaProtocolo categoria;

    private Long consultaId;

    public CategoriaProtocolo getCategoria() { return categoria; }
    public void setCategoria(CategoriaProtocolo categoria) { this.categoria = categoria; }

    public Long getConsultaId() { return consultaId; }
    public void setConsultaId(Long consultaId) { this.consultaId = consultaId; }

    @AssertTrue(message = "Selecione a consulta correspondente à cirurgia.")
    public boolean isConsultaPreenchidaQuandoNecessaria() {
        return categoria != CategoriaProtocolo.POS_CIRURGICO || consultaId != null;
    }
}
