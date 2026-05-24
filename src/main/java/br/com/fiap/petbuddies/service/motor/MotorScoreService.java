package br.com.fiap.petbuddies.service.motor;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import br.com.fiap.petbuddies.domain.entity.FatorRiscoEntity;
import br.com.fiap.petbuddies.domain.entity.ScoreRiscoAnimalEntity;
import br.com.fiap.petbuddies.domain.enums.ClassificacaoRisco;
import br.com.fiap.petbuddies.domain.enums.StatusEventoPlano;
import br.com.fiap.petbuddies.domain.enums.StatusPlano;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import br.com.fiap.petbuddies.domain.enums.TipoRisco;
import br.com.fiap.petbuddies.domain.repository.EventoPlanoRepository;
import br.com.fiap.petbuddies.domain.repository.PlanoCuidadoAnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ScoreRiscoAnimalRepository;
import br.com.fiap.petbuddies.dto.motor.ScoreResponse;
import br.com.fiap.petbuddies.dto.client.AnimalMotorDto;
import br.com.fiap.petbuddies.dto.client.UltimaConsultaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class MotorScoreService {

    private static final int PESO_VACINA_ATRASADA = 40;
    private static final int PESO_CONDICAO_CRONICA = 20;
    private static final int PESO_TEMPO_SEM_CONSULTA = 25;
    private static final int PESO_IDADE_AVANCADA = 15;

    private final ScoreRiscoAnimalRepository scoreRepository;
    private final EventoPlanoRepository eventoPlanoRepository;
    private final PlanoCuidadoAnimalRepository planoRepository;
    private final PetNetApiClient petNetApiClient;

    public MotorScoreService(ScoreRiscoAnimalRepository scoreRepository,
                              EventoPlanoRepository eventoPlanoRepository,
                              PlanoCuidadoAnimalRepository planoRepository,
                              PetNetApiClient petNetApiClient) {
        this.scoreRepository = scoreRepository;
        this.eventoPlanoRepository = eventoPlanoRepository;
        this.planoRepository = planoRepository;
        this.petNetApiClient = petNetApiClient;
    }

    @Transactional
    public ScoreResponse recalcular(Long petNetApiAnimalId, String motivo) {
        Integer scoreAnterior = scoreRepository
                .findScoreMaisRecente(petNetApiAnimalId)
                .map(ScoreRiscoAnimalEntity::getScore)
                .orElse(null);

        Optional<AnimalMotorDto> animalData = petNetApiClient.buscarDadosMotorAnimal(petNetApiAnimalId);
        Optional<UltimaConsultaDto> consultaData = petNetApiClient.buscarUltimaConsulta(petNetApiAnimalId);

        List<FatorRiscoEntity> fatores = List.of(
                calcularVacinaAtrasada(petNetApiAnimalId),
                calcularCondicaoCronica(animalData),
                calcularTempoSemConsulta(consultaData),
                calcularIdadeAvancada(animalData)
        );

        int scoreTotal = Math.min(fatores.stream().mapToInt(FatorRiscoEntity::getValor).sum(), 100);

        ScoreRiscoAnimalEntity scoreEntity = new ScoreRiscoAnimalEntity();
        scoreEntity.setPetNetApiAnimalId(petNetApiAnimalId);
        scoreEntity.setScore(scoreTotal);
        scoreEntity.setClassificacao(ClassificacaoRisco.classificar(scoreTotal));

        fatores.forEach(f -> {
            f.setScoreRisco(scoreEntity);
            f.setContribuicao(scoreTotal > 0
                    ? Math.round((f.getValor() * 100.0 / scoreTotal) * 100.0) / 100.0
                    : 0.0);
        });
        scoreEntity.setFatores(fatores);

        scoreRepository.save(scoreEntity);
        atualizarScoreNoPlano(petNetApiAnimalId, scoreTotal);

        return ScoreResponse.from(scoreEntity, scoreAnterior);
    }

    @Transactional(readOnly = true)
    public Optional<ScoreResponse> buscarMaisRecente(Long petNetApiAnimalId) {
        return scoreRepository
                .findScoreMaisRecente(petNetApiAnimalId)
                .map(ScoreResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ScoreResponse> listarHistorico(Long petNetApiAnimalId, Pageable pageable) {
        return scoreRepository
                .findScoresPorAnimal(petNetApiAnimalId, pageable)
                .map(ScoreResponse::from);
    }

    private FatorRiscoEntity calcularVacinaAtrasada(Long petNetApiAnimalId) {
        long vencidas = eventoPlanoRepository
                .findEventosVencidosPorAnimal(
                        petNetApiAnimalId, TipoEventoProtocolo.VACINACAO,
                        StatusEventoPlano.PENDENTE, LocalDate.now())
                .size();
        int valor = (int) Math.min(vencidas * 20, PESO_VACINA_ATRASADA);
        FatorRiscoEntity f = new FatorRiscoEntity();
        f.setTipo(TipoRisco.VACINA_ATRASADA);
        f.setPeso(PESO_VACINA_ATRASADA);
        f.setValor(valor);
        f.setDescricao(vencidas == 0 ? "Nenhuma vacinação atrasada" : vencidas + " vacinação(ões) com prazo vencido");
        return f;
    }

    private FatorRiscoEntity calcularCondicaoCronica(Optional<AnimalMotorDto> animalData) {
        int valor;
        String descricao;
        if (animalData.isEmpty()) {
            valor = 0;
            descricao = "Dados do .NET indisponíveis";
        } else {
            Boolean cronica = animalData.get().getCondicaoCronica();
            valor = Boolean.TRUE.equals(cronica) ? PESO_CONDICAO_CRONICA : 0;
            descricao = Boolean.TRUE.equals(cronica) ? "Animal com condição crônica registrada" : "Sem condição crônica";
        }
        FatorRiscoEntity f = new FatorRiscoEntity();
        f.setTipo(TipoRisco.CONDICAO_CRONICA);
        f.setPeso(PESO_CONDICAO_CRONICA);
        f.setValor(valor);
        f.setDescricao(descricao);
        return f;
    }

    private FatorRiscoEntity calcularTempoSemConsulta(Optional<UltimaConsultaDto> consultaData) {
        int valor;
        String descricao;
        if (consultaData.isEmpty()) {
            valor = 15;
            descricao = "Dados do .NET indisponíveis — valor padrão conservador";
        } else {
            long dias = ChronoUnit.DAYS.between(consultaData.get().getDataHora().toLocalDate(), LocalDate.now());
            valor = (int) Math.min((dias / 365.0) * PESO_TEMPO_SEM_CONSULTA, PESO_TEMPO_SEM_CONSULTA);
            descricao = dias + " dias desde a última consulta";
        }
        FatorRiscoEntity f = new FatorRiscoEntity();
        f.setTipo(TipoRisco.TEMPO_SEM_CONSULTA);
        f.setPeso(PESO_TEMPO_SEM_CONSULTA);
        f.setValor(valor);
        f.setDescricao(descricao);
        return f;
    }

    private FatorRiscoEntity calcularIdadeAvancada(Optional<AnimalMotorDto> animalData) {
        int valor;
        String descricao;
        if (animalData.isEmpty() || animalData.get().getDataNascimento() == null) {
            valor = 0;
            descricao = "Dados do .NET indisponíveis";
        } else {
            long anos = ChronoUnit.YEARS.between(animalData.get().getDataNascimento(), LocalDate.now());
            if (anos > 7)       { valor = PESO_IDADE_AVANCADA; descricao = "Animal sênior (" + anos + " anos)"; }
            else if (anos >= 5) { valor = 8; descricao = "Animal em fase de envelhecimento (" + anos + " anos)"; }
            else                { valor = 0; descricao = "Animal jovem (" + anos + " anos)"; }
        }
        FatorRiscoEntity f = new FatorRiscoEntity();
        f.setTipo(TipoRisco.IDADE_AVANCADA);
        f.setPeso(PESO_IDADE_AVANCADA);
        f.setValor(valor);
        f.setDescricao(descricao);
        return f;
    }

    private void atualizarScoreNoPlano(Long petNetApiAnimalId, int scoreTotal) {
        planoRepository.findPlanoPorAnimalEStatus(petNetApiAnimalId, StatusPlano.ATIVO)
                .ifPresent(plano -> {
                    plano.setScoreAtual(scoreTotal);
                    plano.setUltimoRecalculo(LocalDateTime.now());
                    planoRepository.save(plano);
                });
    }
}
