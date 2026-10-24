package br.com.fiap.petbuddies.service.cadastro;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.repository.ClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.ConsultaRepository;
import br.com.fiap.petbuddies.dto.cadastro.ContatoClinicaResponse;
import br.com.fiap.petbuddies.web.Formatador;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;

@Service
public class ContatoClinicaService {

    private final ConsultaRepository consultaRepository;
    private final ClinicaRepository clinicaRepository;
    private final Formatador formatador;

    public ContatoClinicaService(ConsultaRepository consultaRepository, ClinicaRepository clinicaRepository,
                                 Formatador formatador) {
        this.consultaRepository = consultaRepository;
        this.clinicaRepository = clinicaRepository;
        this.formatador = formatador;
    }

    /** A clínica de quem prescreveu; sem prescrição, a da última consulta do animal. */
    @Transactional(readOnly = true)
    public ContatoClinicaResponse doAnimal(Long animalId, Collection<PrescricaoEntity> prescricoes) {
        ClinicaEntity clinica = prescricoes.stream()
                .map(p -> p.getVeterinario().getClinica())
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> consultaRepository.findClinicaIdsPorAnimal(animalId, PageRequest.of(0, 1)).stream()
                        .findFirst()
                        .flatMap(clinicaRepository::findById)
                        .orElse(null));
        if (clinica == null) {
            return null;
        }
        String telefone = clinica.getContato() == null ? null : clinica.getContato().getTelefone();
        return new ContatoClinicaResponse(clinica.getNome(), telefone == null ? null : formatador.telefone(telefone));
    }
}
