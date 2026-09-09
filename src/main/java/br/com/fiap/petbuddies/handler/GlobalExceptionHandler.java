package br.com.fiap.petbuddies.handler;

import br.com.fiap.petbuddies.dto.ErrorDto;
import br.com.fiap.petbuddies.exception.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.ClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.CnpjDuplicadoException;
import br.com.fiap.petbuddies.exception.CodigoCondicaoDuplicadoException;
import br.com.fiap.petbuddies.exception.CondicaoClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.ConsultaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.CredenciaisInvalidasException;
import br.com.fiap.petbuddies.exception.CrmvDuplicadoException;
import br.com.fiap.petbuddies.exception.RegraProtocoloNaoEncontradoException;
import br.com.fiap.petbuddies.exception.ResponsavelNaoEncontradoException;
import br.com.fiap.petbuddies.exception.PlanoNaoEncontradoException;
import br.com.fiap.petbuddies.exception.ProtocoloNaoEncontradoException;
import br.com.fiap.petbuddies.exception.VeterinarioNaoEncontradoException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

    // Sem este handler o @ExceptionHandler(Exception.class) abaixo captura antes do Spring MVC e o parametro ausente vira 500.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDto> handleParametroAusente(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(400).body(new ErrorDto("PARAMETRO_OBRIGATORIO",
                "Parâmetro obrigatório ausente: " + ex.getParameterName() + "."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleParametroInvalido(MethodArgumentTypeMismatchException ex) {
        Class<?> type = ex.getRequiredType();
        if (isEnum(type)) {
            return enumInvalido(ex.getName(), ex.getValue(), type);
        }
        return ResponseEntity.status(400).body(new ErrorDto("PARAMETRO_INVALIDO", "Parâmetro inválido: " + ex.getName() + "."));
    }

    /**
     * Sem este mapeamento a recusa de login cairia no tratador generico e
     * viraria 500. Uma mensagem so para os tres casos — login inexistente,
     * senha errada e usuario inativo.
     */
    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorDto> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return ResponseEntity.status(401).body(new ErrorDto("CREDENCIAIS_INVALIDAS", ex.getMessage()));
    }

    @ExceptionHandler(PlanoNaoEncontradoException.class)
    public ResponseEntity<ErrorDto> handlePlanoNaoEncontrado(PlanoNaoEncontradoException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("PLANO_NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(ProtocoloNaoEncontradoException.class)
    public ResponseEntity<ErrorDto> handleProtocoloNaoEncontrado(ProtocoloNaoEncontradoException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("PROTOCOLO_NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(RegraProtocoloNaoEncontradoException.class)
    public ResponseEntity<ErrorDto> handleRegraProtocoloNaoEncontrada(RegraProtocoloNaoEncontradoException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("EVENTO_PROTOCOLO_NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(ClinicaNaoEncontradaException.class)
    public ResponseEntity<ErrorDto> handleClinicaNaoEncontrada(ClinicaNaoEncontradaException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("CLINICA_NAO_ENCONTRADA", ex.getMessage()));
    }

    @ExceptionHandler(ResponsavelNaoEncontradoException.class)
    public ResponseEntity<ErrorDto> handleResponsavelNaoEncontrado(ResponsavelNaoEncontradoException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("RESPONSAVEL_NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(VeterinarioNaoEncontradoException.class)
    public ResponseEntity<ErrorDto> handleVeterinarioNaoEncontrado(VeterinarioNaoEncontradoException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("VETERINARIO_NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(AnimalNaoEncontradoException.class)
    public ResponseEntity<ErrorDto> handleAnimalNaoEncontrado(AnimalNaoEncontradoException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("ANIMAL_NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(ConsultaNaoEncontradaException.class)
    public ResponseEntity<ErrorDto> handleConsultaNaoEncontrada(ConsultaNaoEncontradaException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("CONSULTA_NAO_ENCONTRADA", ex.getMessage()));
    }

    @ExceptionHandler(CondicaoClinicaNaoEncontradaException.class)
    public ResponseEntity<ErrorDto> handleCondicaoClinicaNaoEncontrada(CondicaoClinicaNaoEncontradaException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("CONDICAO_CLINICA_NAO_ENCONTRADA", ex.getMessage()));
    }

    @ExceptionHandler(CnpjDuplicadoException.class)
    public ResponseEntity<ErrorDto> handleCnpjDuplicado(CnpjDuplicadoException ex) {
        return ResponseEntity.status(409).body(new ErrorDto("CNPJ_DUPLICADO", ex.getMessage()));
    }

    @ExceptionHandler(CrmvDuplicadoException.class)
    public ResponseEntity<ErrorDto> handleCrmvDuplicado(CrmvDuplicadoException ex) {
        return ResponseEntity.status(409).body(new ErrorDto("CRMV_DUPLICADO", ex.getMessage()));
    }

    @ExceptionHandler(CodigoCondicaoDuplicadoException.class)
    public ResponseEntity<ErrorDto> handleCodigoCondicaoDuplicado(CodigoCondicaoDuplicadoException ex) {
        return ResponseEntity.status(409).body(new ErrorDto("CODIGO_CONDICAO_DUPLICADO", ex.getMessage()));
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
