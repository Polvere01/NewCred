package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.dto.EnviarVideoResponseDto;
import br.com.newcred.application.usecase.port.IEnviarVideo;
import br.com.newcred.application.usecase.port.IMensagemRepository;
import br.com.newcred.application.usecase.port.IVideoConverter;
import br.com.newcred.application.usecase.port.IWhatsAppCloudApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class EnviarVideo implements IEnviarVideo {

    private final IWhatsAppCloudApiClient metaClient;
    private final IMensagemRepository mensagemRepo;
    private final IVideoConverter videoConverter;

    public EnviarVideo(IWhatsAppCloudApiClient metaClient,
                       IMensagemRepository mensagemRepo,
                       IVideoConverter videoConverter) {
        this.metaClient = metaClient;
        this.mensagemRepo = mensagemRepo;
        this.videoConverter = videoConverter;
    }

    @Override
    @Transactional
    public EnviarVideoResponseDto executar(long conversaId, String waIdDestino, MultipartFile video) {
        if (video == null || video.isEmpty()) throw new IllegalArgumentException("Vídeo vazio");
        if (waIdDestino == null || waIdDestino.isBlank()) throw new IllegalArgumentException("waIdDestino obrigatório");

        // 0) converte para mp4 (H264/AAC) - mais compatível
        var conv = videoConverter.toMp4H264Aac(video);

        // 1) upload
        var mediaId = metaClient.uploadMedia(conv.bytes(), conv.mimeType(), conv.filename());

        // 2) envia video
        var wamid = metaClient.enviarVideoPorMediaId(waIdDestino, mediaId);

        // 3) salva
        mensagemRepo.salvarSaidaMedia(
                conversaId,
                wamid,
                metaClient.getPhoneNumberId(),
                "video",
                mediaId,
                OffsetDateTime.now(ZoneOffset.UTC),
                conv.filename()
        );

        return new EnviarVideoResponseDto(wamid, mediaId);
    }
}