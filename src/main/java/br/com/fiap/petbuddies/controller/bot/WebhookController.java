package br.com.fiap.petbuddies.controller.bot;

import br.com.fiap.petbuddies.dto.evolution.EvolutionWebhookDTO;
import br.com.fiap.petbuddies.service.bot.ChatService;
import br.com.fiap.petbuddies.service.evolution.EvolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "bot — webhook", description = "Recebe eventos da Evolution API (WhatsApp gateway)")
public class WebhookController {

    private final ChatService chatService;
    private final EvolutionService evolutionService;

    public WebhookController(ChatService chatService, EvolutionService evolutionService) {
        this.chatService = chatService;
        this.evolutionService = evolutionService;
    }

    @PostMapping("/webhook/whatsapp")
    @Operation(
        summary = "Webhook Evolution API",
        description = "Recebe eventos de mensagem da Evolution API. "
            + "Sempre retorna 200 para evitar retry infinito — erros são absorvidos internamente. "
            + "Mensagens próprias (fromMe=true) e payloads sem texto são ignorados silenciosamente."
    )
    @ApiResponse(responseCode = "200", description = "Evento recebido (sempre — inclusive em caso de erro interno)")
    public ResponseEntity<Void> receberMensagem(@RequestBody EvolutionWebhookDTO payload) {
        var data = payload.getData();
        if (data == null || data.getKey() == null || data.getKey().isFromMe()) {
            return ResponseEntity.ok().build();
        }
        String remoteJid = data.getKey().getRemoteJid();
        String telefone = remoteJid.replace("@s.whatsapp.net", "");
        String texto = extrairTexto(payload);
        if (texto == null || texto.isBlank()) {
            return ResponseEntity.ok().build();
        }
        try {
            String resposta = chatService.responder(telefone, texto);
            evolutionService.enviarMensagem(remoteJid, resposta);
        } catch (Exception ignored) {
            // Sempre retorna 200 para evitar retry da Evolution API
        }
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleWebhookError(Exception e) {
        return ResponseEntity.ok().build();
    }

    private String extrairTexto(EvolutionWebhookDTO payload) {
        var msg = payload.getData().getMessage();
        if (msg == null) return null;
        if (msg.getConversation() != null) return msg.getConversation();
        if (msg.getExtendedTextMessage() != null) return msg.getExtendedTextMessage().getText();
        return null;
    }
}
