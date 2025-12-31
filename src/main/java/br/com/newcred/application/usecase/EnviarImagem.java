package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.dto.EnviarImagemResponseDto;
import br.com.newcred.application.usecase.port.IEnviarImagem;
import br.com.newcred.application.usecase.port.IMensagemRepository;
import br.com.newcred.application.usecase.port.IWhatsAppCloudApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class EnviarImagem implements IEnviarImagem {

    private final IWhatsAppCloudApiClient metaClient;
    private final IMensagemRepository mensagemRepo;

    public EnviarImagem(IWhatsAppCloudApiClient metaClient,
                        IMensagemRepository mensagemRepo) {
        this.metaClient = metaClient;
        this.mensagemRepo = mensagemRepo;
    }

    @Override
    @Transactional
    public EnviarImagemResponseDto executar(long conversaId, String waIdDestino, MultipartFile imagem, String caption) {
        if (imagem == null || imagem.isEmpty()) throw new IllegalArgumentException("Imagem vazia");
        if (waIdDestino == null || waIdDestino.isBlank()) throw new IllegalArgumentException("waIdDestino obrigatório");

        try {
            String mimeType = imagem.getContentType();
            if (mimeType == null || mimeType.isBlank()) {
                throw new IllegalArgumentException("mimeType não encontrado");
            }

            byte[] bytes = imagem.getBytes();
            String filename = imagem.getOriginalFilename();
            if (filename == null || filename.isBlank()) filename = "image";

            // 1) upload
            var mediaId = metaClient.uploadMedia(bytes, mimeType, filename);

            // 2) envia imagem
            var wamid = metaClient.enviarImagemPorMediaId(waIdDestino, mediaId, caption);

            // 3) salva
            mensagemRepo.salvarSaidaMedia(
                    conversaId,
                    wamid,
                    metaClient.getPhoneNumberId(),
                    "image",
                    mediaId,
                    OffsetDateTime.now(ZoneOffset.UTC),
                    filename
            );

            return new EnviarImagemResponseDto(wamid, mediaId);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar imagem", e);
        }
    }
}