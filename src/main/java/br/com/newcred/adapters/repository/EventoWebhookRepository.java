package br.com.newcred.adapters.repository;

import br.com.newcred.domain.model.EventoWebhook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
@Repository
public class EventoWebhookRepository implements br.com.newcred.application.usecase.port.EventoWebhookRepository {
    private final JdbcTemplate jdbc;

    public EventoWebhookRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long salvar(EventoWebhook evento) {
        String sql = """
        insert into eventos_webhook (tipo_objeto, payload)
        values (?, ?::jsonb)
        returning id
    """;

        Long id = jdbc.queryForObject(
                sql,
                Long.class,
                evento.getTipoObjeto(),
                evento.getPayload()
        );

        return id != null ? id : -1L;
    }

    @Override
    public void marcarProcessado(long id) {
        jdbc.update("update eventos_webhook set processado_em = now(), erro_processamento = null where id = ?", id);
    }

    @Override
    public void marcarErro(long id, String erro) {
        jdbc.update("update eventos_webhook set erro_processamento = ? where id = ?", erro, id);
    }
}
