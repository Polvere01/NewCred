package br.com.newcred.application.usecase.port;

import br.com.newcred.application.usecase.dto.EnviarAudioResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface IEnviarAudio {
    EnviarAudioResponseDto executar(long conversaId, String waIdDestino, MultipartFile audio);
}
