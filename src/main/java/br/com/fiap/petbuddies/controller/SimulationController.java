package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.dto.bot.SimulateMessageRequest;
import br.com.fiap.petbuddies.dto.bot.SimulateMessageResponse;
import br.com.fiap.petbuddies.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "bot")
public class SimulationController {

    private final ChatService chatService;

    public SimulationController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/simulate-message")
    public SimulateMessageResponse simular(@RequestBody @Valid SimulateMessageRequest request) {
        String resposta = chatService.responder(request.getTelefone(), request.getTexto());
        return new SimulateMessageResponse(request.getTelefone(), request.getTexto(), resposta);
    }
}
