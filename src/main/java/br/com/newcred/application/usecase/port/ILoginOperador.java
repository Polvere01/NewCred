package br.com.newcred.application.usecase.port;

import br.com.newcred.application.usecase.dto.LoginRequestDto;
import br.com.newcred.application.usecase.dto.LoginResponseDto;

public interface ILoginOperador {
    LoginResponseDto executar(LoginRequestDto dto);
}
