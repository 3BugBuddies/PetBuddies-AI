package br.com.fiap.petbuddies.client;

import br.com.fiap.petbuddies.dto.client.AnimalMotorDto;
import br.com.fiap.petbuddies.dto.client.ResponsavelDto;
import br.com.fiap.petbuddies.dto.client.UltimaConsultaDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Optional;

@Component
public class PetNetApiClient {

    private final RestClient restClient;

    public PetNetApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    // TODO PRD 04: implementar chamada real a GET /api/responsavel/buscar/{telefone}
    public Optional<ResponsavelDto> buscarResponsavelPorTelefone(String telefone) {
        return Optional.empty();
    }

    // TODO PRD 04: implementar chamada real a GET /api/animal/{id}/motor
    public Optional<AnimalMotorDto> buscarDadosMotorAnimal(Long animalId) {
        return Optional.empty();
    }

    // TODO PRD 04: implementar chamada real a GET /api/animal/{id}/ultima-consulta
    public Optional<UltimaConsultaDto> buscarUltimaConsulta(Long animalId) {
        return Optional.empty();
    }
}
