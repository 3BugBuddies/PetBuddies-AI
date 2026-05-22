package br.com.fiap.petbuddies.flow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
class FlowSessaoHelper {

    private final ObjectMapper objectMapper;

    FlowSessaoHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    String toJson(Object dados) {
        try {
            return objectMapper.writeValueAsString(dados);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Não foi possível serializar dados pendentes", e);
        }
    }

    <T> T lerDados(String json, Class<T> tipo) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, tipo);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
