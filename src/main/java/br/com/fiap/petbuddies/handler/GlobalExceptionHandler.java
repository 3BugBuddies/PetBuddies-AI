package br.com.fiap.petbuddies.handler;

import br.com.fiap.petbuddies.dto.ErrorDto;
import br.com.fiap.petbuddies.exception.ConflitoException;
import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;
import br.com.fiap.petbuddies.exception.checkin.CondicaoObservadaIncoerenteException;
import br.com.fiap.petbuddies.exception.identidade.CredenciaisInvalidasException;
import br.com.fiap.petbuddies.exception.cadastro.ClinicaNaoProvisionadaException;
import br.com.fiap.petbuddies.exception.cadastro.RegistroIncompletoException;
import br.com.fiap.petbuddies.exception.atendimento.JanelaNoPassadoException;
import br.com.fiap.petbuddies.exception.prescricao.RegraPrescricaoIncoerenteException;
import br.com.fiap.petbuddies.exception.cuidado.DuracaoTratamentoExcedeTetoException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // O caminho do campo entra na mensagem porque em corpo de lista a mensagem sozinha
    // e ambigua: "Dose minima nao pode ser maior que a dose maxima" nao diz de qual remedio.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidation(MethodArgumentNotValidException ex) {
        FieldError erro = ex.getBindingResult().getFieldErrors().get(0);
        String msg = erro.getField() + ": " + erro.getDefaultMessage();
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

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorDto> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(404).body(new ErrorDto(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErrorDto> handleConflito(ConflitoException ex) {
        return ResponseEntity.status(409).body(new ErrorDto(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(RegistroIncompletoException.class)
    public ResponseEntity<ErrorDto> handleRegistroIncompleto(RegistroIncompletoException ex) {
        return ResponseEntity.badRequest().body(new ErrorDto("REGISTRO_INCOMPLETO", ex.getMessage()));
    }

    @ExceptionHandler(ClinicaNaoProvisionadaException.class)
    public ResponseEntity<ErrorDto> handleClinicaNaoProvisionada(ClinicaNaoProvisionadaException ex) {
        return ResponseEntity.unprocessableEntity().body(new ErrorDto("CLINICA_NAO_PROVISIONADA", ex.getMessage()));
    }

    @ExceptionHandler(JanelaNoPassadoException.class)
    public ResponseEntity<ErrorDto> handleJanelaNoPassado(JanelaNoPassadoException ex) {
        return ResponseEntity.status(400).body(new ErrorDto("JANELA_NO_PASSADO", ex.getMessage()));
    }

    @ExceptionHandler(RegraPrescricaoIncoerenteException.class)
    public ResponseEntity<ErrorDto> handleRegraPrescricaoIncoerente(RegraPrescricaoIncoerenteException ex) {
        return ResponseEntity.status(400).body(new ErrorDto("REGRA_PRESCRICAO_INCOERENTE", ex.getMessage()));
    }

    @ExceptionHandler(CondicaoObservadaIncoerenteException.class)
    public ResponseEntity<ErrorDto> handleCondicaoObservadaIncoerente(CondicaoObservadaIncoerenteException ex) {
        return ResponseEntity.status(400).body(new ErrorDto("CONDICAO_OBSERVADA_INCOERENTE", ex.getMessage()));
    }

    @ExceptionHandler(DuracaoTratamentoExcedeTetoException.class)
    public ResponseEntity<ErrorDto> handleDuracaoTratamentoExcedeTeto(DuracaoTratamentoExcedeTetoException ex) {
        return ResponseEntity.status(400).body(new ErrorDto("DURACAO_TRATAMENTO_EXCEDE_TETO", ex.getMessage()));
    }

    // Precede o handleGeneric: recurso estatico ausente e 404, nao falha do servidor.
    // Sem isto cada /favicon.ico do navegador vira um ERROR com stacktrace inteiro.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDto> handleRecursoEstaticoAusente(NoResourceFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorDto("RECURSO_NAO_ENCONTRADO", ex.getResourcePath()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGeneric(Exception ex) {
        // sem este log, erro inesperado vira 500 mudo e some do stack
        log.error("Erro nao tratado", ex);
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
