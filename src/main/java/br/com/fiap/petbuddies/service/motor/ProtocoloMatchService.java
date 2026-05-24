package br.com.fiap.petbuddies.service.motor;

import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;
import br.com.fiap.petbuddies.domain.repository.ProtocoloRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProtocoloMatchService {

    private final ProtocoloRepository protocoloRepository;

    public ProtocoloMatchService(ProtocoloRepository protocoloRepository) {
        this.protocoloRepository = protocoloRepository;
    }

    public Optional<ProtocoloEntity> encontrarMelhorMatch(CategoriaProtocolo categoria, Especie especie, Porte porte, Sexo sexo, Boolean castrado, int idadeEmMeses) {

        List<ProtocoloEntity> candidatos = protocoloRepository
                .findByCategoriaAndEspecieAndAtivoTrue(categoria, especie);

        return candidatos.stream()
                .filter(p -> aceitaPorte(p, porte))
                .filter(p -> aceitaSexo(p, sexo))
                .filter(p -> aceitaCastrado(p, castrado))
                .filter(p -> aceitaIdade(p, idadeEmMeses))
                .max(Comparator.comparingInt(this::calcularEspecificidade)
                        .thenComparing(p -> p.getCreatedAt() != null ? p.getCreatedAt() : java.time.LocalDateTime.MIN));
    }

    private boolean aceitaPorte(ProtocoloEntity p, Porte porte) {
        return p.getPorte() == null || p.getPorte() == porte;
    }

    private boolean aceitaSexo(ProtocoloEntity p, Sexo sexo) {
        return p.getSexo() == null || p.getSexo() == sexo;
    }

    private boolean aceitaCastrado(ProtocoloEntity p, Boolean castrado) {
        return p.getCastrado() == null || p.getCastrado().equals(castrado);
    }

    private boolean aceitaIdade(ProtocoloEntity p, int idadeEmMeses) {
        boolean minOk = p.getIdadeMinMeses() == null || idadeEmMeses >= p.getIdadeMinMeses();
        boolean maxOk = p.getIdadeMaxMeses() == null || idadeEmMeses <= p.getIdadeMaxMeses();
        return minOk && maxOk;
    }

    private int calcularEspecificidade(ProtocoloEntity p) {
        int score = 0;
        if (p.getPorte() != null)         score++;
        if (p.getSexo() != null)          score++;
        if (p.getCastrado() != null)      score++;
        if (p.getIdadeMinMeses() != null) score++;
        if (p.getIdadeMaxMeses() != null) score++;
        return score;
    }
}
