package br.com.newcred.adapters.repository;

import br.com.newcred.application.usecase.port.IContatoRepository;
import br.com.newcred.domain.model.Contato;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ContatoRepository implements IContatoRepository {

    private final JdbcTemplate jdbc;

    public ContatoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long upsertERetornarId(String whatsappId, String nome) {
        String sql = """
            insert into contatos (whatsapp_id, nome)
            values (?, ?)
            on conflict (whatsapp_id)
            do update set
              nome = coalesce(excluded.nome, contatos.nome),
              atualizado_em = now()
            returning id
        """;

        Long id = jdbc.queryForObject(sql, Long.class, whatsappId, nome);
        return id != null ? id : -1L;
    }
}
