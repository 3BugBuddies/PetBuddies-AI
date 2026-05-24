package br.com.fiap.petbuddies.handler;

import br.com.fiap.petbuddies.dto.ErrorDto;
import br.com.fiap.petbuddies.exception.EventoProtocoloNaoEncontradoException;
import br.com.fiap.petbuddies.exception.PlanoNaoEncontradoException;
import br.com.fiap.petbuddies.exception.ProtocoloNaoEncontradoException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(400).body(new ErrorDto("VALIDACAO_INVALIDA", msg));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleJsonInvalido(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException invalidFormat && isEnum(invalidFormat.getTargetType())) {
            String field = invalidFormat.getPath().isEmpty()
                    ? "campo"
                    : invalidFormat.getPath().stream()
                            .map(JsonMappingException.Reference::getFieldName)
                            .filter(name -> name != null && !name.isBlank())
                            .reduce((first, second) -> second)
                            .orElse("campo");
            return enumInvalido(field, invalidFormat.getValue(), invalidFormat.getTargetType());
        }
        return ResponseEntity.status(400).body(new ErrorDto("JSON_INVALIDO", "Corpo da requisição inválido."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleParametroInvalido(MethodArgumentTypeMismatchException ex) {
        Class<?> type = ex.getRequiredType();
        if (isEnum(type)) {
            return enumInvalido(ex.getName(), ex.getValue(), type);
        }
        return ResponseEntity.status(400).body(new ErrorDto("PARAMETRO_INVALIDO", "Parâmetro inválido: " + ex.getName() + "."));
    }

    @ExceptionHandler(PlanoNaoEncontradoException.class)
    public ResponseEntity<ErrorDto> handlePlanoNaoEncontrado(PlanoNaoEncontradoException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("PLANO_NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(ProtocoloNaoEncontradoException.class)
    public ResponseEntity<ErrorDto> handleProtocoloNaoEncontrado(ProtocoloNaoEncontradoException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("PROTOCOLO_NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(EventoProtocoloNaoEncontradoException.class)
    public ResponseEntity<ErrorDto> handleEventoProtocoloNaoEncontrado(EventoProtocoloNaoEncontradoException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("EVENTO_PROTOCOLO_NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGeneric(Exception ex) {
        return ResponseEntity.status(500).body(new ErrorDto("ERRO_INTERNO", "Erro inesperado no servidor."));
    }

    private static ResponseEntity<ErrorDto> enumInvalido(String campo, Object valor, Class<?> enumType) {
        String valoresAceitos = Arrays.stream(enumType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        String msg = "Valor inválido para " + campo + ": " + valor + ". Valores aceitos: " + valoresAceitos + ".";
        return ResponseEntity.status(400).body(new ErrorDto("VALOR_ENUM_INVALIDO", msg));
    }

    private static boolean isEnum(Class<?> type) {
        return type != null && type.isEnum();
    }
}
