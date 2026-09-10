package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;
import br.com.fiap.petbuddies.domain.enums.cadastro.Porte;
import br.com.fiap.petbuddies.domain.enums.cadastro.Sexo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnimalRequest {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
    private String nome;

    @NotNull(message = "Espécie é obrigatória.")
    private Especie especie;

    @Size(max = 100, message = "Raça deve ter no máximo 100 caracteres.")
    private String raca;

    @NotNull(message = "Porte é obrigatório.")
    private Porte porte;

    @NotNull(message = "Sexo é obrigatório.")
    private Sexo sexo;

    @NotNull(message = "Data de nascimento é obrigatória.")
    @PastOrPresent(message = "Data de nascimento não pode ser futura.")
    private LocalDate dataNascimento;

    // NR_PESO é NUMBER(5,2): três dígitos inteiros e duas casas.
    @DecimalMin(value = "0.0", message = "Peso não pode ser negativo.")
    @DecimalMax(value = "999.99", message = "Peso deve ser no máximo 999,99.")
    private BigDecimal peso;

    private Boolean condicaoCronica;

    private Boolean castrado;

    @Size(max = 500, message = "Foto deve ter no máximo 500 caracteres.")
    private String foto;

    @Size(max = 2000, message = "Alergias devem ter no máximo 2000 caracteres.")
    private String alergias;

    @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres.")
    private String observacoes;

    @NotNull(message = "Responsável é obrigatório.")
    private Long responsavelId;
}
