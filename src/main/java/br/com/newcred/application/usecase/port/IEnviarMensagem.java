package br.com.newcred.application.usecase.port;

import br.com.newcred.application.usecase.dto.MensagensRequestDto;
import br.com.newcred.application.usecase.dto.MensagensResponseDto;

public interface IEnviarMensagem {

    MensagensResponseDto enviar(MensagensRequestDto dto);
}
