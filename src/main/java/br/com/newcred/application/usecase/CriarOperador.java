package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.dto.CriarOperadorRequestDto;
import br.com.newcred.application.usecase.dto.CriarOperadorResponseDto;
import br.com.newcred.application.usecase.port.ICriarOperador;
import br.com.newcred.application.usecase.port.IOperadorRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class CriarOperador implements ICriarOperador {

    private final IOperadorRepository operadorRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public CriarOperador(IOperadorRepository operadorRepository) {
        this.operadorRepository = operadorRepository;
    }

    @Override
    public CriarOperadorResponseDto executar(CriarOperadorRequestDto dto) {

        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("nome é obrigatório");
        }
        if (dto.email() == null || dto.email().isBlank()) {
            throw new IllegalArgumentException("email é obrigatório");
        }
        if (dto.senha() == null || dto.senha().length() < 6) {
            throw new IllegalArgumentException("senha deve ter pelo menos 6 caracteres");
        }

        String email = dto.email().trim().toLowerCase();

        if (operadorRepository.existePorEmail(email)) {
            throw new IllegalArgumentException("email já cadastrado");
        }

        String role = (dto.role() == null || dto.role().isBlank())
                ? "OPERADOR"
                : dto.role().trim().toUpperCase();

        if (!role.equals("OPERADOR") && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("role inválida");
        }

        String hash = encoder.encode(dto.senha());

        Long id = operadorRepository.inserir(dto.nome().trim(), email, hash, role);

        var op = operadorRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalStateException("operador não encontrado após inserir"));

        return new CriarOperadorResponseDto(op.id(), op.nome(), op.email(), op.role(), op.ativo());
    }
}