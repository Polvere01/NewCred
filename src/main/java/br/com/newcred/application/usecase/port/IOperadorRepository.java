package br.com.newcred.application.usecase.port;

import java.util.Optional;

public interface IOperadorRepository {
    boolean existePorEmail(String email);

    Long inserir(String nome, String email, String senhaHash, String role);

    Optional<OperadorResumo> buscarPorId(Long id);

    Optional<OperadorAuth> buscarPorEmail(String email);

    record OperadorResumo(Long id, String nome, String email, String role, Boolean ativo) {}

    record OperadorAuth(
            Long id,
            String nome,
            String email,
            String senhaHash,
            String role,
            Boolean ativo
    ) {}
}
