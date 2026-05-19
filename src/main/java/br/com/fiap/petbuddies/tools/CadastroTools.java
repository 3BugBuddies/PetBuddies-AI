package br.com.fiap.petbuddies.tools;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import br.com.fiap.petbuddies.dto.client.AnimalDto;
import br.com.fiap.petbuddies.dto.client.CadastrarAnimalRequest;
import br.com.fiap.petbuddies.dto.client.CadastrarResponsavelRequest;
import br.com.fiap.petbuddies.dto.client.ResponsavelDto;
import br.com.fiap.petbuddies.exception.PetNetApiConflictException;
import br.com.fiap.petbuddies.exception.PetNetApiUnavailableException;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CadastroTools {

    private final PetNetApiClient petNetApiClient;

    public CadastroTools(PetNetApiClient petNetApiClient) {
        this.petNetApiClient = petNetApiClient;
    }

    @Tool(description = "Busca um responsável pelo número de telefone do WhatsApp")
    public String buscarResponsavelPorTelefone(String telefone) {
        return petNetApiClient.buscarResponsavelPorTelefone(telefone)
                .map(r -> "OK|id=" + r.getId() + "|nome=" + r.getNome() + "|status=" + r.getStatus())
                .orElse("VAZIO|responsável não encontrado para este telefone");
    }

    @Tool(description = "Cadastra novo responsável com nome e telefone")
    public String cadastrarResponsavel(String nome, String telefone) {
        try {
            ResponsavelDto r = petNetApiClient.cadastrarResponsavel(
                    new CadastrarResponsavelRequest(nome, telefone));
            return "OK|id=" + r.getId() + "|nome=" + r.getNome();
        } catch (PetNetApiConflictException e) {
            return "BLOQUEADO|tutor já cadastrado com este telefone — use buscarResponsavelPorTelefone";
        } catch (PetNetApiUnavailableException e) {
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
        try {
            AnimalDto a = petNetApiClient.cadastrarAnimal(
                    new CadastrarAnimalRequest(responsavelId, nome, especie, porte, sexo, castrado, dataNascimento));
            return "OK|id=" + a.getId() + "|nome=" + a.getNome() + "|plano_preventivo=solicitado";
        } catch (PetNetApiUnavailableException e) {
            return "ERRO|serviço indisponível, tente novamente";
        }
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
