package br.com.fiap.petbuddies.dto.paciente;

import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;
import br.com.fiap.petbuddies.domain.enums.cadastro.Sexo;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaPlano;
import java.time.LocalDate;

/** Uma linha da lista de pacientes; plano e próximo cuidado nulos quando o animal não tem plano ativo. */
public record PacienteResumoResponse(
        Long id,
        String nome,
        Especie especie,
        String raca,
        Sexo sexo,
        String responsavelNome,
        String responsavelTelefone,
        CategoriaPlano planoCategoria,
        LocalDate planoAte,
        String proximoCuidado,
        LocalDate proximoCuidadoData,
        boolean proximoCuidadoVencido) {
}
