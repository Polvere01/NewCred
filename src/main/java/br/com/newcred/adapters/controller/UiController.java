package br.com.newcred.adapters.controller;

import br.com.newcred.adapters.dto.ConversaListaDTO;
import br.com.newcred.adapters.dto.MensagemDTO;
import br.com.newcred.adapters.repository.ConversaListaRepository;
import br.com.newcred.application.usecase.dto.MensagensRequestDto;
import br.com.newcred.application.usecase.dto.MensagensResponseDto;
import br.com.newcred.application.usecase.port.IBaixarMidiaMensagem;
import br.com.newcred.application.usecase.port.IEnviarMensagem;
import br.com.newcred.application.usecase.port.IMensagemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/conversas")
public class UiController {

    private final ConversaListaRepository repo;
    private final IEnviarMensagem enviarMensagemUseCase;
    private final IBaixarMidiaMensagem baixarMidia;

    //TODO remover esse repository
    private final IMensagemRepository mensagemRepository;

    public UiController(ConversaListaRepository repo, IEnviarMensagem enviarMensagemUseCase, IMensagemRepository mensagemRepository, IBaixarMidiaMensagem baixarMidia) {
        this.repo = repo;
        this.enviarMensagemUseCase = enviarMensagemUseCase;
        this.mensagemRepository = mensagemRepository;
        this.baixarMidia = baixarMidia;
    }

    @GetMapping
    public List<ConversaListaDTO> listar() {
        return repo.listar();
    }

    @GetMapping("/{id}/mensagens")
    public List<MensagemDTO> listar(@PathVariable("id") long conversaId) {
        return mensagemRepository.listarPorConversa(conversaId);
    }

    @PostMapping("/mensagens/enviar")
    public MensagensResponseDto enviar(@RequestBody MensagensRequestDto req) {
        return enviarMensagemUseCase.enviar(req);
    }


    @GetMapping("/{id}/media")
    public ResponseEntity<StreamingResponseBody> baixar(@PathVariable long id) {

        var midiaDto = baixarMidia.executar(id);

        StreamingResponseBody body = out -> {
            try (var in = midiaDto.stream()) {
                in.transferTo(out);
            }
        };

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(midiaDto.mimeType()))
                .header("Cache-Control", "no-store")
                .body(body);
    }

}