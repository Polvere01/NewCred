package br.com.newcred.application.usecase.port;

import java.util.List;
import java.util.Optional;

public interface IOperadorRepository {
    boolean existePorEmail(String email);

    Long inserir(String nome, String email, String senhaHash, String role, Long supervisorId);

    Optional<OperadorResumo> buscarPorId(Long id);

    Optional<OperadorAuth> buscarPorEmail(String email);

    void inserirVinculos(long operadorId, List<String> phoneNumberIds);

    record OperadorResumo(Long id, String nome, String email, String role, Boolean ativo, Long supervisorId) {}

    record OperadorAuth(
            Long id,
            String nome,
            String email,
            String senhaHash,
            String role,
            Boolean ativo
    ) {}
}
