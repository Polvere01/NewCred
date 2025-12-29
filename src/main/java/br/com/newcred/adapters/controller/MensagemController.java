package br.com.newcred.adapters.controller;

import br.com.newcred.application.usecase.dto.MensagensRequestDto;
import br.com.newcred.application.usecase.dto.MensagensResponseDto;
import br.com.newcred.application.usecase.port.IEnviarMensagem;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MensagemController {

    private final IEnviarMensagem enviarMensagemUseCase;

    public MensagemController(IEnviarMensagem enviarMensagemUseCase) {
        this.enviarMensagemUseCase = enviarMensagemUseCase;
    }

    @PostMapping("/mensagens/enviar")
    public MensagensResponseDto enviar(@RequestBody MensagensRequestDto req) {
        return enviarMensagemUseCase.enviar(req);
    }
}