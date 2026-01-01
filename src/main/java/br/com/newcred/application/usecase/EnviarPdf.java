package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.dto.EnviarPdfResponseDto;
import br.com.newcred.application.usecase.port.IEnviarPdf;
import br.com.newcred.application.usecase.port.IMensagemRepository;
import br.com.newcred.application.usecase.port.IWhatsAppCloudApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class EnviarPdf implements IEnviarPdf {

    private final IWhatsAppCloudApiClient metaClient;
    private final IMensagemRepository mensagemRepo;

    public EnviarPdf(IWhatsAppCloudApiClient metaClient, IMensagemRepository mensagemRepo) {
        this.metaClient = metaClient;
        this.mensagemRepo = mensagemRepo;
    }

    @Override
    @Transactional
    public EnviarPdfResponseDto executar(long conversaId, String waIdDestino, MultipartFile pdf, String caption) {
        if (pdf == null || pdf.isEmpty()) throw new IllegalArgumentException("PDF vazio");
        if (waIdDestino == null || waIdDestino.isBlank()) throw new IllegalArgumentException("waIdDestino obrigatório");

        try {
            String mimeType = pdf.getContentType();
            if (mimeType == null || mimeType.isBlank()) mimeType = "application/pdf";

            // opcional: garantir que é pdf mesmo
            if (!mimeType.equalsIgnoreCase("application/pdf")) {
                throw new IllegalArgumentException("Arquivo não parece PDF (content-type=" + mimeType + ")");
            }

            byte[] bytes = pdf.getBytes();
            String filename = pdf.getOriginalFilename();
            if (filename == null || filename.isBlank()) filename = "documento.pdf";
            if (!filename.toLowerCase().endsWith(".pdf")) filename = filename + ".pdf";

            // 1) upload
            var mediaId = metaClient.uploadMedia(bytes, mimeType, filename);

            // 2) envia documento
            var wamid = metaClient.enviarDocumentoPorMediaId(waIdDestino, mediaId, filename, caption);

            // 3) salva
            var mensagemId = mensagemRepo.salvarSaidaMedia(
                    conversaId,
                    wamid,
                    metaClient.getPhoneNumberId(),
                    "document",
                    mediaId,
                    OffsetDateTime.now(ZoneOffset.UTC),
                    filename
            );

            return new EnviarPdfResponseDto(wamid, mediaId, String.valueOf(mensagemId));

        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar PDF", e);
        }
    }
}