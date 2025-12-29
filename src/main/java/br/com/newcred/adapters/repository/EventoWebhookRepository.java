package br.com.newcred.adapters.repository;

import br.com.newcred.domain.model.EventoWebhook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class EventoWebhookRepository implements br.com.newcred.application.usecase.port.IEventoWebhookRepository {
    private final JdbcTemplate jdbc;

    public EventoWebhookRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long salvar(EventoWebhook evento) {
        // DICA: aqui é melhor usar RETURNING id pra evitar esse erro de KeyHolder com múltiplas chaves
        String sql = """
            insert into eventos_webhook (tipo_objeto, payload)
            values (?, ?::jsonb)
            returning id
        """;

        Long id = jdbc.queryForObject(sql, Long.class, evento.getTipoObjeto(), evento.getPayload());
        return id != null ? id : -1L;
    }

    @Override
    public List<EventoWebhook>  buscarPendentesParaProcessar(int limite) {
        // pega um lote de eventos que não foram processados e não estão "reservados"
        // e já reserva marcando processando_em
        String sql = """
            with selecionados as (
                select id
                from eventos_webhook
                where processado_em is null
                  and (processando_em is null or processando_em < now() - interval '5 minutes')
                order by id
                for update skip locked
                limit ?
            )
            update eventos_webhook e
            set processando_em = now(),
                erro_processamento = null
            from selecionados s
            where e.id = s.id
            returning e.id, e.tipo_objeto, e.payload::text, e.recebido_em
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            long id = rs.getLong("id");
            String tipoObjeto = rs.getString("tipo_objeto");
            String payload = rs.getString("payload");
            OffsetDateTime recebidoEm = rs.getObject("recebido_em", OffsetDateTime.class);
            return EventoWebhook.existente(id, tipoObjeto, payload, recebidoEm);
        }, limite);
    }

    @Override
    public void marcarProcessado(long id) {
        jdbc.update("""
            update eventos_webhook
            set processado_em = now(),
                processando_em = null,
                erro_processamento = null
            where id = ?
        """, id);
    }

    @Override
    public void marcarErro(long id, String erro) {
        jdbc.update("""
            update eventos_webhook
            set erro_processamento = ?,
                processando_em = null
            where id = ?
        """, erro, id);
    }
}
