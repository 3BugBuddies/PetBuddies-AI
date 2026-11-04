package br.com.fiap.petbuddies.exception.pontuacao;

// O PetBuddies-API (.NET) não respondeu, ou respondeu com erro que não é do usuário: vira 503.
public class PontuacaoIndisponivelException extends RuntimeException {

    public PontuacaoIndisponivelException(Throwable causa) {
        super("Pontuação indisponível no momento. Tente novamente em instantes.", causa);
    }
}
