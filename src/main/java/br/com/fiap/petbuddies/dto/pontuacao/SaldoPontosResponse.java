package br.com.fiap.petbuddies.dto.pontuacao;

// Lido do PetBuddies-API (.NET): o saldo é a soma dos lançamentos do responsável.
public record SaldoPontosResponse(Long responsavelId, int saldo) {
}
