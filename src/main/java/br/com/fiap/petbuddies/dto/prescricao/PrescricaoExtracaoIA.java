package br.com.fiap.petbuddies.dto.prescricao;

import java.util.List;

/** Envelope devolvido pelo modelo: um item por medicamento, na ordem da narrativa. */
public record PrescricaoExtracaoIA(List<MedicamentoExtraidoIA> medicamentos) {
}
