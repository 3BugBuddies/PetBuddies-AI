package br.com.fiap.petbuddies.tools;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import br.com.fiap.petbuddies.dto.client.AgendarConsultaRequest;
import br.com.fiap.petbuddies.dto.client.CancelarConsultaRequest;
import br.com.fiap.petbuddies.dto.client.ConsultaDto;
import br.com.fiap.petbuddies.dto.client.JanelaAtendimentoDto;
import br.com.fiap.petbuddies.exception.PetNetApiUnavailableException;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AgendamentoTools {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private final PetNetApiClient petNetApiClient;

    public AgendamentoTools(PetNetApiClient petNetApiClient) {
        this.petNetApiClient = petNetApiClient;
    }

    @Tool(description = "Lista as janelas de atendimento disponíveis na clínica")
    public String listarJanelasDisponiveis() {
        try {
            List<JanelaAtendimentoDto> janelas = petNetApiClient.listarJanelasDisponiveis();
            if (janelas.isEmpty()) return "VAZIO|nenhum horário disponível no momento";
            String lista = janelas.stream()
                    .limit(5)
                    .map(j -> j.getId() + "="
                            + DATA_HORA.format(j.getDataHoraInicio()) + "-"
                            + DATA_HORA.format(j.getDataHoraFim())
                            + " com " + nomeOuPadrao(j.getVeterinarioNome()))
                    .collect(Collectors.joining("; "));
            return "OK|" + lista;
        } catch (PetNetApiUnavailableException e) {
            return "ERRO|serviço clínico indisponível para listar horários";
        }
    }

    @Tool(description = "Agenda uma consulta para o animal em uma janela de atendimento")
    public String agendarConsulta(Long animalId, Long janelaId, String tipoConsulta) {
        try {
            String tipo = (tipoConsulta == null || tipoConsulta.isBlank())
                    ? "ROTINA" : tipoConsulta.trim().toUpperCase();
            ConsultaDto c = petNetApiClient.agendarConsulta(
                    new AgendarConsultaRequest(animalId, janelaId, tipo));
            return "OK|consultaId=" + c.getId()
                    + "|tipo=" + c.getTipoConsulta()
                    + "|dataHora=" + DATA_HORA.format(c.getDataHora())
                    + "|status=" + c.getStatus();
        } catch (PetNetApiUnavailableException e) {
            return "ERRO|não foi possível agendar: " + e.getMessage();
        }
    }

    @Tool(description = "Lista as consultas agendadas de um animal")
    public String listarConsultasDoAnimal(Long animalId) {
        try {
            List<ConsultaDto> consultas = petNetApiClient.listarConsultasDoAnimal(animalId);
            if (consultas.isEmpty()) return "VAZIO|nenhuma consulta encontrada para este animal";
            String lista = consultas.stream()
                    .limit(5)
                    .map(c -> c.getId() + "=" + c.getTipoConsulta()
                            + " em " + DATA_HORA.format(c.getDataHora())
                            + " (" + c.getStatus() + ")")
                    .collect(Collectors.joining("; "));
            return "OK|" + lista;
        } catch (PetNetApiUnavailableException e) {
            return "ERRO|serviço clínico indisponível para listar consultas";
        }
    }

    @Tool(description = "Cancela uma consulta agendada informando o motivo")
    public String cancelarConsulta(Long consultaId, String motivo) {
        try {
            String m = (motivo == null || motivo.isBlank())
                    ? "Tutor solicitou via WhatsApp" : motivo;
            ConsultaDto c = petNetApiClient.cancelarConsulta(consultaId, new CancelarConsultaRequest(m));
            return "OK|consultaId=" + c.getId() + "|status=" + c.getStatus();
        } catch (PetNetApiUnavailableException e) {
            return "ERRO|não foi possível cancelar: " + e.getMessage();
        }
    }

    private static String nomeOuPadrao(String nome) {
        return (nome == null || nome.isBlank()) ? "veterinário da clínica" : nome;
    }
}
