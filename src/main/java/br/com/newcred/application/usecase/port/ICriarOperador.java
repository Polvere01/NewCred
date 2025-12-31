package br.com.newcred.application.usecase.port;

import br.com.newcred.application.usecase.dto.CriarOperadorRequestDto;
import br.com.newcred.application.usecase.dto.CriarOperadorResponseDto;

public interface ICriarOperador {
    CriarOperadorResponseDto executar(CriarOperadorRequestDto dto);
}
