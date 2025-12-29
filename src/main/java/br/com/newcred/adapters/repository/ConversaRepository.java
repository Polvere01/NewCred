package br.com.newcred.adapters.repository;

import br.com.newcred.application.usecase.port.IConversaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public class ConversaRepository implements IConversaRepository {


    private final JdbcTemplate jdbc;

    public ConversaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long criarOuAtualizar(long contatoId, OffsetDateTime ultimaMensagemEm) {
        String sql = """
                    insert into conversas (contato_id, status, ultima_mensagem_em)
                    values (?, 'ABERTA', ?)
                    on conflict (contato_id)
                    do update set
                      ultima_mensagem_em = excluded.ultima_mensagem_em,
                      atualizado_em = now()
                    returning id
                """;

        Long id = jdbc.queryForObject(sql, Long.class, contatoId, ultimaMensagemEm);
        return id != null ? id : -1L;
    }
}
