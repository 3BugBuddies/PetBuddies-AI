package br.com.fiap.petbuddies.tools;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import br.com.fiap.petbuddies.dto.client.AnimalDto;
import br.com.fiap.petbuddies.dto.client.CadastrarAnimalRequest;
import br.com.fiap.petbuddies.dto.client.CadastrarResponsavelRequest;
import br.com.fiap.petbuddies.dto.client.ResponsavelDto;
import br.com.fiap.petbuddies.exception.PetNetApiConflictException;
import br.com.fiap.petbuddies.exception.PetNetApiUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CadastroTools {

    private static final Logger log = LoggerFactory.getLogger(CadastroTools.class);

    private final PetNetApiClient petNetApiClient;

    public CadastroTools(PetNetApiClient petNetApiClient) {
        this.petNetApiClient = petNetApiClient;
    }

    @Tool(description = "Busca um responsável pelo número de telefone do WhatsApp")
    public String buscarResponsavelPorTelefone(String telefone) {
        try {
            String resultado = petNetApiClient.buscarResponsavelPorTelefone(telefone)
                    .map(r -> "OK|id=" + r.getId() + "|nome=" + r.getNome() + "|status=" + r.getStatus())
                    .orElse("VAZIO|responsável não encontrado para este telefone");
            log.debug("[TOOL] buscarResponsavel tel={} → {}", telefone, resultado);
            return resultado;
        } catch (PetNetApiUnavailableException e) {
            log.error("[TOOL] buscarResponsavel ERRO tel={}: {}", telefone, e.getMessage());
            return "ERRO|serviço indisponível, tente novamente";
        }
    }

    @Tool(description = "Cadastra novo responsável com nome e telefone")
    public String cadastrarResponsavel(String nome, String telefone) {
        log.debug("[TOOL] cadastrarResponsavel nome={} tel={}", nome, telefone);
        try {
            ResponsavelDto r = petNetApiClient.cadastrarResponsavel(
                    new CadastrarResponsavelRequest(nome, telefone));
            String resultado = "OK|id=" + r.getId() + "|nome=" + r.getNome();
            log.debug("[TOOL] cadastrarResponsavel → {}", resultado);
            return resultado;
        } catch (PetNetApiConflictException e) {
            log.debug("[TOOL] cadastrarResponsavel BLOQUEADO tel={}", telefone);
            return "BLOQUEADO|tutor já cadastrado com este telefone — use buscarResponsavelPorTelefone";
        } catch (PetNetApiUnavailableException e) {
            log.error("[TOOL] cadastrarResponsavel ERRO: {}", e.getMessage(), e);
            return "ERRO|serviço indisponível, tente novamente";
        }
    }

    @Tool(description = "Cadastra animal vinculado ao responsável. "
            + "especie: CACHORRO, GATO, PASSARO, COELHO, HAMSTER, OUTRO. "
            + "porte: MINI, PEQUENO, MEDIO, GRANDE, GIGANTE. "
            + "sexo: MACHO, FEMEA. "
            + "dataNascimento: formato yyyy-MM-dd.")
    public String cadastrarAnimal(Long responsavelId, String nome, String especie,
                                   String porte, String sexo, boolean castrado, String dataNascimento) {
        log.debug("[TOOL] cadastrarAnimal responsavelId={} nome={} especie={} porte={} sexo={} castrado={} dataNascimento={}",
                responsavelId, nome, especie, porte, sexo, castrado, dataNascimento);
        try {
            AnimalDto a = petNetApiClient.cadastrarAnimal(new CadastrarAnimalRequest(
                    responsavelId, nome,
                    normalizar(especie),
                    normalizar(porte),
                    normalizar(sexo),
                    castrado,
                    normalizarData(dataNascimento)));
            String resultado = "OK|id=" + a.getId() + "|nome=" + a.getNome() + "|plano_preventivo=solicitado";
            log.debug("[TOOL] cadastrarAnimal → {}", resultado);
            return resultado;
        } catch (PetNetApiUnavailableException e) {
            log.error("[TOOL] cadastrarAnimal ERRO responsavelId={}: {}", responsavelId, e.getMessage(), e);
            return "ERRO|serviço indisponível, tente novamente";
        }
    }

    private static String normalizar(String valor) {
        return valor == null ? null : valor.trim().toUpperCase();
    }

    private static String normalizarData(String data) {
        if (data == null) return null;
        // dd/MM/yyyy → yyyy-MM-dd
        if (data.matches("\\d{2}/\\d{2}/\\d{4}")) {
            String[] p = data.split("/");
            return p[2] + "-" + p[1] + "-" + p[0];
        }
        // d/M/yyyy (sem zero) → yyyy-MM-dd
        if (data.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            String[] p = data.split("/");
            return p[2] + "-" + String.format("%02d", Integer.parseInt(p[1]))
                         + "-" + String.format("%02d", Integer.parseInt(p[0]));
        }
        return data;
    }

    @Tool(description = "Lista todos os animais cadastrados de um responsável")
    public String listarAnimaisDoResponsavel(Long responsavelId) {
        try {
            List<AnimalDto> animais = petNetApiClient.listarAnimaisDoResponsavel(responsavelId);
            if (animais.isEmpty()) return "VAZIO|nenhum animal cadastrado";
            String lista = animais.stream()
                    .map(a -> a.getId() + "=" + a.getNome() + "(" + a.getEspecie() + ")")
                    .collect(Collectors.joining(", "));
            return "OK|" + lista;
        } catch (PetNetApiUnavailableException e) {
            return "ERRO|serviço indisponível";
        }
    }
}
