package br.com.fiap.petbuddies.dto.atendimento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * O que o modelo semanal abriria: {@code inicios} são só as janelas novas; as que já existem e as que caem
 * em dia bloqueado ficam de fora e são contadas à parte.
 */
public record PrevisaoJanelas(List<LocalDateTime> inicios, int jaExistem, int bloqueadas, LocalDate de, LocalDate ate) {

    public int aAbrir() {
        return inicios.size();
    }
}
