package br.com.fiap.petbuddies.dto.cadastro;

import java.util.List;

/** Uma linha da lista de responsáveis do painel: contato, animais e se tem login ativo no app. */
public record ResponsavelResumoDto(
        Long id,
        String nome,
        String telefone,
        String email,
        List<String> animais,
        boolean acessoApp) {
}
