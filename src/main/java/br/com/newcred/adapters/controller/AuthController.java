package br.com.newcred.adapters.controller;

import br.com.newcred.application.usecase.LoginOperador;
import br.com.newcred.application.usecase.dto.LoginRequestDto;
import br.com.newcred.application.usecase.dto.LoginResponseDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginOperador login;

    public AuthController(LoginOperador login) {
        this.login = login;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto dto) {
        return login.executar(dto);
    }
}