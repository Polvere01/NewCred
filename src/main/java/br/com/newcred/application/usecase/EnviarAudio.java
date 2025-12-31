package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.dto.EnviarAudioResponseDto;
import br.com.newcred.application.usecase.port.IAudioConverter;
import br.com.newcred.application.usecase.port.IEnviarAudio;
import br.com.newcred.application.usecase.port.IMensagemRepository;
import br.com.newcred.application.usecase.port.IWhatsAppCloudApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class EnviarAudio implements IEnviarAudio {

    private final IWhatsAppCloudApiClient metaClient;
    private final IMensagemRepository mensagemRepo;
    private final IAudioConverter audioConverter;

    public EnviarAudio(IWhatsAppCloudApiClient metaClient,
                       IMensagemRepository mensagemRepo,
                       IAudioConverter audioConverter) {
        this.metaClient = metaClient;
        this.mensagemRepo = mensagemRepo;
        this.audioConverter = audioConverter;
    }

    @Override
    @Transactional
    public EnviarAudioResponseDto executar(long conversaId, String waIdDestino, MultipartFile audio) {
        if (audio == null || audio.isEmpty()) throw new IllegalArgumentException("Áudio vazio");
        if (waIdDestino == null || waIdDestino.isBlank()) throw new IllegalArgumentException("waIdDestino obrigatório");

        // 0) converte (webm -> ogg/opus)
        var conv = audioConverter.webmToOggOpus(audio);

        // 1) upload pra Meta -> mediaId
        var mediaId = metaClient.uploadMedia(conv.bytes(), conv.mimeType(), conv.filename());

        // 2) envia msg de audio -> wamid
        var wamid = metaClient.enviarAudioPorMediaId(waIdDestino, mediaId);

        // 3) salva no banco (OUT)
        mensagemRepo.salvarSaidaMedia(
                conversaId,
                wamid,
                metaClient.getPhoneNumberId(),
                "audio",
                mediaId,
                OffsetDateTime.now(ZoneOffset.UTC),
                conv.filename() // ou audio.getOriginalFilename(), mas conv é mais seguro
        );

        return new EnviarAudioResponseDto(wamid, mediaId);
    }
}