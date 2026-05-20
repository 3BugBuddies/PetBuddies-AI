package br.com.fiap.petbuddies.tools;

import br.com.fiap.petbuddies.domain.entity.NotificacaoEntity;
import br.com.fiap.petbuddies.domain.entity.TriagemSessaoEntity;
import br.com.fiap.petbuddies.domain.enums.CanalNotificacao;
import br.com.fiap.petbuddies.domain.enums.ClassificacaoTriagem;
import br.com.fiap.petbuddies.domain.enums.StatusNotificacao;
import br.com.fiap.petbuddies.domain.repository.NotificacaoRepository;
import br.com.fiap.petbuddies.domain.repository.TriagemSessaoRepository;
import br.com.fiap.petbuddies.service.EvolutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class TriagemTools {

    private static final Logger log = LoggerFactory.getLogger(TriagemTools.class);

    private final TriagemSessaoRepository triagemSessaoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final EvolutionService evolutionService;

    @Value("${clinica.telefone:}")
    private String clinicaTelefone;

    public TriagemTools(TriagemSessaoRepository triagemSessaoRepository,
                        NotificacaoRepository notificacaoRepository,
                        EvolutionService evolutionService) {
        this.triagemSessaoRepository = triagemSessaoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.evolutionService = evolutionService;
    }

    @Tool(description = "Inicia uma sessão de triagem para o animal, registrando o sintoma principal")
    public String iniciarTriagem(String telefone, Long petNetApiAnimalId, String sintomaPrincipal) {
        try {
            Optional<TriagemSessaoEntity> existente = triagemSessaoRepository
                    .findFirstByTelefoneAndFinalizadaEmIsNullOrderByIniciadaEmDesc(telefone);
            if (existente.isPresent()) {
                return "OK|sessaoId=" + existente.get().getId() + "|status=EM_ANDAMENTO";
            }
            TriagemSessaoEntity sessao = new TriagemSessaoEntity();
            sessao.setTelefone(telefone);
            sessao.setPetNetApiAnimalId(petNetApiAnimalId);
            sessao.setSintomaPrincipal(sintomaPrincipal);
            triagemSessaoRepository.save(sessao);
            log.info("[TRIAGEM] sessao iniciada id={} animalId={}", sessao.getId(), petNetApiAnimalId);
            return "OK|sessaoId=" + sessao.getId() + "|status=INICIADA";
        } catch (Exception e) {
            log.error("[TRIAGEM] erro ao iniciar tel={}: {}", telefone, e.getMessage());
            return "ERRO|não foi possível iniciar a sessão de triagem";
        }
    }

    @Tool(description = "Finaliza a triagem com classificação (PODE_ESPERAR, PRIORITARIO ou EMERGENCIA) e recomendação. Dispara alerta à clínica em EMERGENCIA.")
    public String finalizarTriagem(String telefone, String classificacao, String recomendacao) {
        try {
            TriagemSessaoEntity sessao = triagemSessaoRepository
                    .findFirstByTelefoneAndFinalizadaEmIsNullOrderByIniciadaEmDesc(telefone)
                    .orElse(null);
            if (sessao == null) return "ERRO|nenhuma sessão de triagem ativa para este telefone";

            ClassificacaoTriagem classif = ClassificacaoTriagem.valueOf(classificacao.trim().toUpperCase());
            sessao.setClassificacao(classif);
            sessao.setRecomendacao(recomendacao);
            sessao.setFinalizadaEm(LocalDateTime.now());

            if (classif == ClassificacaoTriagem.EMERGENCIA) {
                enviarAlertaClinica(sessao);
                sessao.setAlertaEnviado(true);
            }

            triagemSessaoRepository.save(sessao);
            log.info("[TRIAGEM] sessao finalizada id={} classificacao={}", sessao.getId(), classif);
            return "OK|sessaoId=" + sessao.getId() + "|classificacao=" + classif;
        } catch (IllegalArgumentException e) {
            return "ERRO|classificacao inválida — use PODE_ESPERAR, PRIORITARIO ou EMERGENCIA";
        } catch (Exception e) {
            log.error("[TRIAGEM] erro ao finalizar tel={}: {}", telefone, e.getMessage());
            return "ERRO|não foi possível finalizar a triagem";
        }
    }

    private void enviarAlertaClinica(TriagemSessaoEntity sessao) {
        String payload = "🔴 EMERGÊNCIA — triagem #" + sessao.getId()
                + " | Sintoma: " + sessao.getSintomaPrincipal()
                + " | Animal petNetId=" + sessao.getPetNetApiAnimalId();

        NotificacaoEntity alerta = new NotificacaoEntity();
        alerta.setPetNetApiAnimalId(sessao.getPetNetApiAnimalId() != null ? sessao.getPetNetApiAnimalId() : 0L);
        alerta.setCanal(CanalNotificacao.WHATSAPP);
        alerta.setPayload(payload);

        if (clinicaTelefone != null && !clinicaTelefone.isBlank()) {
            try {
                evolutionService.enviarMensagem(clinicaTelefone + "@s.whatsapp.net", payload);
                alerta.setStatusEnvio(StatusNotificacao.ENVIADA);
                log.info("[TRIAGEM] alerta emergência enviado para clínica");
            } catch (Exception e) {
                alerta.setStatusEnvio(StatusNotificacao.FALHOU);
                log.warn("[TRIAGEM] falha ao enviar alerta: {}", e.getMessage());
            }
        } else {
            alerta.setStatusEnvio(StatusNotificacao.IGNORADA);
            log.warn("[TRIAGEM] clinica.telefone não configurado — alerta criado como IGNORADA");
        }
        notificacaoRepository.save(alerta);
    }
}
