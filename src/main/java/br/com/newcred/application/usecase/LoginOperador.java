package br.com.newcred.application.usecase;

import br.com.newcred.adapters.config.security.JwtService;
import br.com.newcred.adapters.dto.OperadorPrincipalDto;
import br.com.newcred.application.usecase.dto.LoginRequestDto;
import br.com.newcred.application.usecase.dto.LoginResponseDto;
import br.com.newcred.application.usecase.dto.UserDto;
import br.com.newcred.application.usecase.port.ILoginOperador;

import br.com.newcred.application.usecase.port.IOperadorRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginOperador implements ILoginOperador {

    private final IOperadorRepository repo;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public LoginOperador(IOperadorRepository repo, JwtService jwtService) {
        this.repo = repo;
        this.jwtService = jwtService;
    }

    public LoginResponseDto executar(LoginRequestDto dto) {
        String email = dto.email().trim().toLowerCase();

        var op = repo.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("credenciais inválidas"));

        if (!op.ativo()) throw new IllegalArgumentException("usuário inativo");
        if (!encoder.matches(dto.senha(), op.senhaHash())) {
            throw new IllegalArgumentException("credenciais inválidas");
        }

        var principal = new OperadorPrincipalDto(op.id(), op.email(), op.role());
        String token = jwtService.gerarToken(principal);

        return new LoginResponseDto(
                token,
                new UserDto(op.id(), op.nome(), op.email(), op.role())
        );
    }
}
