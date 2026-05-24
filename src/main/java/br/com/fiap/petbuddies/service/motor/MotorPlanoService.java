package br.com.fiap.petbuddies.service.motor;

import br.com.fiap.petbuddies.domain.entity.EventoPlanoEntity;
import br.com.fiap.petbuddies.domain.entity.EventoProtocoloEntity;
import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoAnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.StatusEventoPlano;
import br.com.fiap.petbuddies.domain.enums.StatusPlano;
import br.com.fiap.petbuddies.domain.repository.EventoPlanoRepository;
import br.com.fiap.petbuddies.domain.repository.PlanoCuidadoAnimalRepository;
import br.com.fiap.petbuddies.dto.motor.EventoPlanoDto;
import br.com.fiap.petbuddies.dto.motor.PlanoPreventivoRequest;
import br.com.fiap.petbuddies.dto.motor.PlanoPosCirurgicoRequest;
import br.com.fiap.petbuddies.dto.motor.PlanoResponse;
import br.com.fiap.petbuddies.exception.PlanoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Function;

@Service
public class MotorPlanoService {

    private final PlanoCuidadoAnimalRepository planoRepository;
    private final EventoPlanoRepository eventoPlanoRepository;
    private final ProtocoloMatchService protocoloMatchService;

    public MotorPlanoService(PlanoCuidadoAnimalRepository planoRepository,
                             EventoPlanoRepository eventoPlanoRepository,
                             ProtocoloMatchService protocoloMatchService) {
        this.planoRepository = planoRepository;
        this.eventoPlanoRepository = eventoPlanoRepository;
        this.protocoloMatchService = protocoloMatchService;
    }

    @Transactional
    public PlanoResponse instanciarPreventivo(PlanoPreventivoRequest req) {
        Optional<PlanoCuidadoAnimalEntity> existente = planoRepository
                .findPlanoAtivoPorCategoria(
                        req.getPetNetApiAnimalId(), StatusPlano.ATIVO, CategoriaProtocolo.PREVENTIVO);

        if (existente.isPresent()) {
            return PlanoResponse.from(existente.get(), false, "PLANO_JA_EXISTENTE");
        }

        int idadeEmMeses = (int) ChronoUnit.MONTHS.between(req.getDataNascimento(), LocalDate.now());

        Optional<ProtocoloEntity> protocolo = protocoloMatchService.encontrarMelhorMatch(
                CategoriaProtocolo.PREVENTIVO,
                req.getEspecie(), req.getPorte(), req.getSexo(), req.getCastrado(), idadeEmMeses);

        if (protocolo.isEmpty()) {
            return PlanoResponse.semProtocolo();
        }

        PlanoCuidadoAnimalEntity plano = criarPlano(req.getPetNetApiAnimalId(), null, protocolo.get());
        instanciarEventos(plano, protocolo.get(),
                ep -> LocalDate.now().plusMonths(ep.getMesAplicacao() != null ? ep.getMesAplicacao() : 0));
        planoRepository.save(plano);

        return PlanoResponse.from(plano, true, null);
    }

    @Transactional
    public PlanoResponse instanciarPosCirurgico(PlanoPosCirurgicoRequest req) {
        Optional<PlanoCuidadoAnimalEntity> existente = planoRepository
                .findPlanoPorAnimalEConsulta(
                        req.getPetNetApiAnimalId(), req.getPetNetApiConsultaId());

        if (existente.isPresent()) {
            return PlanoResponse.from(existente.get(), false, "PLANO_JA_EXISTENTE");
        }

        Optional<ProtocoloEntity> protocolo = protocoloMatchService.encontrarMelhorMatch(
                CategoriaProtocolo.POS_CIRURGICO, req.getEspecie(), null, null, null, 0);

        if (protocolo.isEmpty()) {
            return PlanoResponse.semProtocolo();
        }

        LocalDate dataBase = req.getDataRealizacao().toLocalDate();
        PlanoCuidadoAnimalEntity plano = criarPlano(
                req.getPetNetApiAnimalId(), req.getPetNetApiConsultaId(), protocolo.get());
        instanciarEventos(plano, protocolo.get(), ep -> dataBase.plusDays(ep.getDiasAposInicio()));
        planoRepository.save(plano);

        return PlanoResponse.from(plano, true, null);
    }

    @Transactional(readOnly = true)
    public Optional<PlanoResponse> buscarPlanoAtivo(Long petNetApiAnimalId) {
        return planoRepository
                .findPlanoAtivoPorCategoria(
                        petNetApiAnimalId, StatusPlano.ATIVO, CategoriaProtocolo.PREVENTIVO)
                .map(PlanoResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<EventoPlanoDto> listarEventos(Long petNetApiAnimalId, Pageable pageable) {
        return eventoPlanoRepository
                .findEventosPorAnimal(petNetApiAnimalId, pageable)
                .map(EventoPlanoDto::from);
    }

    @Transactional
    public void cancelarPlano(Long planoId, String motivo) {
        PlanoCuidadoAnimalEntity plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new PlanoNaoEncontradoException(planoId));
        plano.setStatus(StatusPlano.CANCELADO);
        plano.setCanceladoEm(LocalDateTime.now());
        plano.setMotivoCancelamento(motivo);
        plano.getEventos().stream()
                .filter(e -> e.getStatus() == StatusEventoPlano.PENDENTE)
                .forEach(e -> e.setStatus(StatusEventoPlano.CANCELADO));
        planoRepository.save(plano);
    }

    private PlanoCuidadoAnimalEntity criarPlano(Long petNetApiAnimalId, Long petNetApiConsultaId,
                                                 ProtocoloEntity protocolo) {
        PlanoCuidadoAnimalEntity plano = new PlanoCuidadoAnimalEntity();
        plano.setPetNetApiAnimalId(petNetApiAnimalId);
        plano.setPetNetApiConsultaId(petNetApiConsultaId);
        plano.setProtocolo(protocolo);
        plano.setStatus(StatusPlano.ATIVO);
        return plano;
    }

    private void instanciarEventos(PlanoCuidadoAnimalEntity plano, ProtocoloEntity protocolo,
                                    Function<EventoProtocoloEntity, LocalDate> dataAlvoFn) {
        for (EventoProtocoloEntity ep : protocolo.getEventos()) {
            EventoPlanoEntity evento = new EventoPlanoEntity();
            evento.setPlano(plano);
            evento.setEventoProtocolo(ep);
            evento.setTipo(ep.getTipo());
            evento.setNome(ep.getNome());
            evento.setDataAlvo(dataAlvoFn.apply(ep));
            evento.setStatus(StatusEventoPlano.PENDENTE);
            evento.setTentativas(0);
            plano.getEventos().add(evento);
        }
    }
}
