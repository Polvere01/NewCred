package br.com.newcred.application.usecase.port;

import br.com.newcred.application.usecase.dto.EnviarVideoResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface IEnviarVideo {
    EnviarVideoResponseDto executar(long conversaId, String waIdDestino, MultipartFile video, String phoneNumberId);
}
