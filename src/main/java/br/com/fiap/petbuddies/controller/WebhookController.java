package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.dto.evolution.EvolutionWebhookDTO;
import br.com.fiap.petbuddies.service.ChatService;
import br.com.fiap.petbuddies.service.EvolutionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "bot")
public class WebhookController {

    private final ChatService chatService;
    private final EvolutionService evolutionService;

    public WebhookController(ChatService chatService, EvolutionService evolutionService) {
        this.chatService = chatService;
        this.evolutionService = evolutionService;
    }

    @PostMapping("/webhook/whatsapp")
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
