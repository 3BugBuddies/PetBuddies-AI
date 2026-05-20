package br.com.fiap.petbuddies.client;

import br.com.fiap.petbuddies.dto.client.*;
import br.com.fiap.petbuddies.exception.PetNetApiConflictException;
import br.com.fiap.petbuddies.exception.PetNetApiUnavailableException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
public class PetNetApiClient {

    private final RestClient restClient;

    public PetNetApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<ResponsavelDto> buscarResponsavelPorTelefone(String telefone) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/api/responsavel/buscar/{t}", telefone)
                    .retrieve().body(ResponsavelDto.class));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) return Optional.empty();
            throw new PetNetApiUnavailableException(e);
        } catch (Exception e) {
            throw new PetNetApiUnavailableException(e);
        }
    }

    public ResponsavelDto cadastrarResponsavel(CadastrarResponsavelRequest req) {
        try {
            return restClient.post()
                    .uri("/api/responsavel")
                    .body(req)
                    .retrieve().body(ResponsavelDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 409)
                throw new PetNetApiConflictException("Telefone já cadastrado");
            throw new PetNetApiUnavailableException(e);
        } catch (Exception e) {
            throw new PetNetApiUnavailableException(e);
        }
    }

    public AnimalDto cadastrarAnimal(CadastrarAnimalRequest req) {
        try {
            return restClient.post()
                    .uri("/api/animal")
                    .body(req)
                    .retrieve().body(AnimalDto.class);
        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            if (status == 400) throw new PetNetApiUnavailableException("combinação especie/porte sem TipoAnimal cadastrado: " + e.getResponseBodyAsString());
            if (status == 404) throw new PetNetApiUnavailableException("responsável não encontrado no sistema clínico");
            throw new PetNetApiUnavailableException(e);
        } catch (Exception e) {
            throw new PetNetApiUnavailableException(e);
        }
    }

    public List<AnimalDto> listarAnimaisDoResponsavel(Long responsavelId) {
        try {
            List<AnimalDto> result = restClient.get()
                    .uri("/api/responsavel/{id}/animal", responsavelId)
                    .retrieve().body(new ParameterizedTypeReference<List<AnimalDto>>() {});
            return result != null ? result : List.of();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) return List.of();
            throw new PetNetApiUnavailableException(e);
        } catch (Exception e) {
            throw new PetNetApiUnavailableException(e);
        }
    }

    public List<JanelaAtendimentoDto> listarJanelasDisponiveis() {
        try {
            List<JanelaAtendimentoDto> result = restClient.get()
                    .uri("/api/janela-atendimento")
                    .retrieve().body(new ParameterizedTypeReference<List<JanelaAtendimentoDto>>() {});
            return result != null ? result : List.of();
        } catch (Exception e) {
            throw new PetNetApiUnavailableException(e);
        }
    }

    public ConsultaDto agendarConsulta(AgendarConsultaRequest req) {
        try {
            return restClient.post()
                    .uri("/api/consulta")
                    .body(req)
                    .retrieve().body(ConsultaDto.class);
        } catch (HttpClientErrorException e) {
            throw new PetNetApiUnavailableException(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new PetNetApiUnavailableException(e);
        }
    }

    public List<ConsultaDto> listarConsultasDoAnimal(Long animalId) {
        try {
            List<ConsultaDto> result = restClient.get()
                    .uri("/api/consulta?animalId={id}", animalId)
                    .retrieve().body(new ParameterizedTypeReference<List<ConsultaDto>>() {});
            return result != null ? result : List.of();
        } catch (Exception e) {
            throw new PetNetApiUnavailableException(e);
        }
    }

    public ConsultaDto cancelarConsulta(Long consultaId, CancelarConsultaRequest req) {
        try {
            return restClient.patch()
                    .uri("/api/consulta/{id}", consultaId)
                    .body(req)
                    .retrieve().body(ConsultaDto.class);
        } catch (HttpClientErrorException e) {
            throw new PetNetApiUnavailableException(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new PetNetApiUnavailableException(e);
        }
    }

    public Optional<AnimalMotorDto> buscarDadosMotorAnimal(Long animalId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/api/animal/{id}/motor", animalId)
                    .retrieve().body(AnimalMotorDto.class));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) return Optional.empty();
            throw new PetNetApiUnavailableException(e);
        } catch (Exception e) {
            throw new PetNetApiUnavailableException(e);
        }
    }

    public Optional<UltimaConsultaDto> buscarUltimaConsulta(Long animalId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/api/animal/{id}/ultima-consulta", animalId)
                    .retrieve().body(UltimaConsultaDto.class));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) return Optional.empty();
            throw new PetNetApiUnavailableException(e);
        } catch (Exception e) {
            throw new PetNetApiUnavailableException(e);
        }
    }
}
