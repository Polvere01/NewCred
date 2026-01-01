package br.com.newcred.adapters.controller;

import br.com.newcred.adapters.config.security.OperadorContext;
import br.com.newcred.adapters.dto.ConversaListaDTO;
import br.com.newcred.adapters.dto.MensagemDTO;
import br.com.newcred.adapters.repository.ConversaListaRepository;
import br.com.newcred.application.usecase.dto.MensagensRequestDto;
import br.com.newcred.application.usecase.dto.MensagensResponseDto;
import br.com.newcred.application.usecase.dto.CriarOperadorRequestDto;
import br.com.newcred.application.usecase.dto.CriarOperadorResponseDto;
import br.com.newcred.application.usecase.port.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conversas")
public class UiController {

    private final ConversaListaRepository repo;
    private final IEnviarMensagem enviarMensagemUseCase;
    private final IBaixarMidiaMensagem baixarMidia;
    private final IEnviarAudio enviarAudio;
    private final IEnviarVideo enviarVideo;
    private final IEnviarImagem enviarImagem;
    private final IEnviarPdf enviarPdf;

    //TODO remover esse repository
    private final IMensagemRepository mensagemRepository;

    public UiController(ConversaListaRepository repo, IEnviarMensagem enviarMensagemUseCase, IBaixarMidiaMensagem baixarMidia, IEnviarAudio enviarAudio, IMensagemRepository mensagemRepository, IEnviarVideo enviarVideo, IEnviarImagem enviarImagem, IEnviarPdf enviarPdfUseCase) {
        this.enviarPdf = enviarPdfUseCase;
        this.enviarImagem = enviarImagem;
        this.enviarVideo = enviarVideo;
        this.repo = repo;
        this.enviarMensagemUseCase = enviarMensagemUseCase;
        this.baixarMidia = baixarMidia;
        this.enviarAudio = enviarAudio;
        this.mensagemRepository = mensagemRepository;
    }

    @GetMapping
    public List<ConversaListaDTO> listar() {
        long operadorId = OperadorContext.getOperadorId();
        return repo.listarPorOperador(operadorId);
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
    public ResponseEntity<InputStreamResource> baixar(@PathVariable long id) {
        var midiaDto = baixarMidia.executar(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(midiaDto.mimeType()))
                .header("Cache-Control", "no-store")
                .body(new InputStreamResource(midiaDto.stream()));
    }


    @PostMapping(value = "/{conversaId}/mensagens/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> enviarAudio(
            @PathVariable long conversaId,
            @RequestParam("waIdDestino") String waIdDestino,
            @RequestPart("audio") MultipartFile audio
    ) {
        var resp = enviarAudio.executar(conversaId, waIdDestino, audio);
        return Map.of(
                "wamid", resp.wamid(),
                "mediaId", resp.mediaId()
        );
    }


    @PostMapping(value = "/{conversaId}/mensagens/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> enviarVideo(
            @PathVariable long conversaId,
            @RequestParam("waIdDestino") String waIdDestino,
            @RequestPart("video") MultipartFile video
    ) {
        var resp = enviarVideo.executar(conversaId, waIdDestino, video);
        return Map.of(
                "wamid", resp.wamid(),
                "mediaId", resp.mediaId()
        );
    }

    @PostMapping(value = "/{conversaId}/mensagens/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> enviarImagem(
            @PathVariable long conversaId,
            @RequestParam("waIdDestino") String waIdDestino,
            @RequestPart("imagem") MultipartFile imagem,
            @RequestParam(value = "caption", required = false) String caption
    ) {
        var resp = enviarImagem.executar(conversaId, waIdDestino, imagem, caption);
        return Map.of(
                "wamid", resp.wamid(),
                "mediaId", resp.mediaId()
        );
    }

    @PostMapping(value = "/{conversaId}/mensagens/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> enviarPdf(
            @PathVariable long conversaId,
            @RequestParam("waIdDestino") String waIdDestino,
            @RequestPart("pdf") MultipartFile pdf,
            @RequestParam(value = "caption", required = false) String caption
    ) {
        var resp = enviarPdf.executar(conversaId, waIdDestino, pdf, caption);
        return Map.of(
                "wamid", resp.wamid(),
                "mediaId", resp.mediaId()
        );
    }

}