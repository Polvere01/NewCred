package br.com.newcred.application.usecase.port;

import br.com.newcred.application.usecase.dto.EnviarImagemResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface IEnviarImagem {
    EnviarImagemResponseDto executar(long conversaId, String waIdDestino, MultipartFile imagem, String caption, String phoneNumberId);

}
