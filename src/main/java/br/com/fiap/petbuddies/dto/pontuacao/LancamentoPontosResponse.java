package br.com.fiap.petbuddies.dto.pontuacao;

import java.time.LocalDateTime;

// Um lançamento do extrato, lido do PetBuddies-API (.NET): quantos pontos o responsável ganhou numa consulta.
public record LancamentoPontosResponse(
        Long id,
        Long consultaId,
        Long responsavelId,
        Long animalId,
        Long clinicaId,
        LocalDateTime dataConsulta,
        int pontos,
        LocalDateTime createdAt) {
}
