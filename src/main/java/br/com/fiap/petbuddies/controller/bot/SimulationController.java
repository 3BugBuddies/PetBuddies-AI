package br.com.fiap.petbuddies.controller.bot;

import br.com.fiap.petbuddies.dto.bot.SimulateMessageRequest;
import br.com.fiap.petbuddies.dto.bot.SimulateMessageResponse;
import br.com.fiap.petbuddies.service.bot.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "bot — simulação", description = "Simulação de mensagens WhatsApp para testes sem Evolution API")
public class SimulationController {

    private final ChatService chatService;

    public SimulationController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/simulate-message")
    @Operation(
        summary = "Simular mensagem WhatsApp",
        description = "Envia uma mensagem diretamente ao ChatService sem passar pela Evolution API. "
            + "Útil para testar o bot localmente via Postman/Swagger. "
            + "A memória de conversa é mantida por telefone, igual ao fluxo real."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resposta gerada pelo bot"),
        @ApiResponse(responseCode = "400", description = "telefone ou texto ausentes")
    })
    public SimulateMessageResponse simular(@RequestBody @Valid SimulateMessageRequest request) {
        String resposta = chatService.responder(request.getTelefone(), request.getTexto());
        return new SimulateMessageResponse(request.getTelefone(), request.getTexto(), resposta);
    }
}
