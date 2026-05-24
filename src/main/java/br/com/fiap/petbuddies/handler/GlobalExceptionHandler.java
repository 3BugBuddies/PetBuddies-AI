package br.com.fiap.petbuddies.handler;

import br.com.fiap.petbuddies.dto.ErrorDto;
import br.com.fiap.petbuddies.exception.EventoProtocoloNaoEncontradoException;
import br.com.fiap.petbuddies.exception.PlanoNaoEncontradoException;
import br.com.fiap.petbuddies.exception.ProtocoloNaoEncontradoException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(400).body(new ErrorDto("VALIDACAO_INVALIDA", msg));
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
}
