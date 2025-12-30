package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.dto.ResultadoMidiaDto;
import br.com.newcred.application.usecase.port.IBaixarMidiaMensagem;
import br.com.newcred.application.usecase.port.IMensagemRepository;
import br.com.newcred.application.usecase.port.IMetaMediaGateway;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;

@Service
public class BaixarMidiaMensagem implements IBaixarMidiaMensagem {

    private final IMensagemRepository mensagemRepo;
    private final IMetaMediaGateway meta;

    public BaixarMidiaMensagem(IMensagemRepository mensagemRepo, IMetaMediaGateway meta) {
        this.mensagemRepo = mensagemRepo;
        this.meta = meta;
    }

    @Override
    public ResultadoMidiaDto executar(long mensagemId) {
        var info = mensagemRepo.buscarMediaInfo(mensagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não existe"));

        if (info.mediaId() == null || info.mediaId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mensagem não possui media_id");
        }

        var mediaInfo = meta.obterMediaInfo(info.mediaId());
        InputStream stream = meta.baixarMediaStream(mediaInfo.url());

        String mime = (mediaInfo.mimeType() != null && !mediaInfo.mimeType().isBlank())
                ? mediaInfo.mimeType()
                : "application/octet-stream";

        return new ResultadoMidiaDto(mime, stream);
    }
}