package br.com.newcred.adapters.repository;

import br.com.newcred.application.usecase.port.IOperadorRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OperadorRepository implements IOperadorRepository {

    private final JdbcTemplate jdbc;

    public OperadorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existePorEmail(String email) {
        String sql = "select exists(select 1 from operadores where email = ?)";
        Boolean exists = jdbc.queryForObject(sql, Boolean.class, email);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Long inserir(String nome, String email, String senhaHash, String role, Long supervisorId) {
        String sql = """
            insert into operadores (nome, email, senha_hash, role, ativo, supervisor_id)
            values (?, ?, ?, ?, true, ?)
            returning id
        """;
        return jdbc.queryForObject(sql, Long.class, nome, email, senhaHash, role, supervisorId);
    }

    @Override
    public Optional<OperadorResumo> buscarPorId(Long id) {
        String sql = """
            select id, nome, email, role, ativo, supervisor_id
            from operadores
            where id = ?
        """;

        return jdbc.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(new OperadorResumo(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getBoolean("ativo"),
                    rs.getLong("supervisor_id")
            ));
        }, id);
    }

    @Override
    public Optional<OperadorAuth> buscarPorEmail(String email) {

        String sql = """
        select id, nome, email, senha_hash, role, ativo
        from operadores
        where email = ?
    """;

        return jdbc.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(new OperadorAuth(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("senha_hash"),
                    rs.getString("role"),
                    rs.getBoolean("ativo")
            ));
        }, email);
    }
}