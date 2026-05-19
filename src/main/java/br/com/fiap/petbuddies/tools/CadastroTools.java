package br.com.fiap.petbuddies.tools;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

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

    @Tool(description = "Cadastra um novo responsável com nome e telefone")
    public String cadastrarResponsavel(String nome, String telefone) {
        return "STUB|cadastrarResponsavel não implementado ainda (PRD 05)";
    }

    @Tool(description = "Cadastra um animal vinculado a um responsável já cadastrado")
    public String cadastrarAnimal(Long responsavelId, String nome, String especie,
                                   String porte, String sexo, boolean castrado, String dataNascimento) {
        return "STUB|cadastrarAnimal não implementado ainda (PRD 05)";
    }

    @Tool(description = "Lista todos os animais cadastrados de um responsável")
    public String listarAnimaisDoResponsavel(Long responsavelId) {
        return "STUB|listarAnimaisDoResponsavel não implementado ainda (PRD 05)";
    }
}
