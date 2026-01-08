package br.com.newcred.application.usecase.port;

import br.com.newcred.application.usecase.dto.EnviarPdfResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface IEnviarPdf {
    EnviarPdfResponseDto executar(long conversaId, String waIdDestino, MultipartFile pdf, String caption, String phoneNumberId);
}
