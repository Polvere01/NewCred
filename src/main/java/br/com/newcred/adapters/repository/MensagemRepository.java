package br.com.newcred.adapters.repository;

import br.com.newcred.adapters.dto.MensagemDTO;
import br.com.newcred.application.usecase.dto.MensagemMediaInfoDto;
import br.com.newcred.application.usecase.port.IMensagemRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MensagemRepository implements IMensagemRepository {

    private final JdbcTemplate jdbc;

    public MensagemRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    @Override
    public void salvarEntrada(long conversaId, String wamid, String whatsappIdOrigem,
                              String texto, long timestampWhatsapp, OffsetDateTime enviadoEm) {

        String sql = """
                    insert into mensagens (
                        conversa_id, whatsapp_message_id, direcao,
                        whatsapp_id_origem, phone_number_id_destino,
                        tipo, texto, timestamp_whatsapp, enviado_em
                    )
                    values (?, ?, 'IN', ?, null, 'text', ?, ?, ?)
                    on conflict (whatsapp_message_id)
                    do nothing
                """;

        jdbc.update(sql,
                conversaId,
                wamid,
                whatsappIdOrigem,
                texto,
                timestampWhatsapp,
                enviadoEm
        );
    }

    @Override
    public void salvarEntradaMedia(
            long conversaId,
            String wamid,
            String whatsappIdOrigem,
            String tipo,
            String mediaId,
            long timestampWhatsapp,
            OffsetDateTime enviadoEm,
            String filename
    ) {
        String sql = """
                    insert into mensagens (
                        conversa_id,
                        whatsapp_message_id,
                        direcao,
                        whatsapp_id_origem,
                        tipo,
                        media_id,
                        nome_arquivo,
                        timestamp_whatsapp,
                        enviado_em
                    )
                    values (?, ?, 'IN', ?, ?, ?, ?, ?, ?)
                    on conflict (whatsapp_message_id)
                    do nothing
                """;

        jdbc.update(
                sql,
                conversaId,
                wamid,
                whatsappIdOrigem,
                tipo,
                mediaId,
                filename,
                timestampWhatsapp,
                enviadoEm
        );
    }

    @Override
    public void salvarSaida(
            Long conversaId,
            String wamid,
            String phoneNumberIdDestino,
            String texto
    ) {

            String sql = """
                    insert into mensagens (
                        conversa_id,
                        whatsapp_message_id,
                        direcao,
                        whatsapp_id_origem,
                        phone_number_id_destino,
                        tipo,
                        texto,
                        enviado_em,
                        status
                    )
                    values (?, ?, 'OUT', null, ?, 'text', ?, now(), 'sent')
                    on conflict (whatsapp_message_id)
                    do nothing
                """;

        jdbc.update(
                sql,
                conversaId,
                wamid,
                phoneNumberIdDestino,
                texto
        );
    }

    public List<MensagemDTO> listarPorConversa(long conversaId) {
        String sql = """
                    select
                        id,
                        texto,
                        tipo,
                        direcao,
                        enviado_em,
                        status
                    from mensagens
                    where conversa_id = ?
                    order by enviado_em asc, id asc
                """;

        return jdbc.query(sql, (rs, i) -> {
            String dirDb = rs.getString("direcao"); // IN / OUT
            String dirApi = "IN".equalsIgnoreCase(dirDb) ? "ENTRADA" : "SAIDA";

            return new MensagemDTO(
                    rs.getLong("id"),
                    rs.getString("texto"),
                    rs.getString("tipo"),
                    dirApi,
                    rs.getObject("enviado_em", OffsetDateTime.class),
                    rs.getString("status")
            );
        }, conversaId);
    }

    @Override
    public Optional<MensagemMediaInfoDto> buscarMediaInfo(long mensagemId) {
        String sql = """
                    select media_id, tipo
                    from mensagens
                    where id = ?
                """;

        return jdbc.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(new MensagemMediaInfoDto(
                    rs.getString("media_id"),
                    rs.getString("tipo")
            ));
        }, mensagemId);
    }

    @Override
    public void atualizarStatusPorWamid(String wamid, String status, long statusTs, OffsetDateTime statusEm) {
        // evita regredir status (read > delivered > sent)
        String sql = """
                    update mensagens
                    set
                      status = case
                        when status = 'read' then status
                        when ? = 'read' then 'read'
                        when status = 'delivered' then status
                        when ? = 'delivered' then 'delivered'
                        when status is null then ?
                        else status
                      end,
                      status_ts = greatest(coalesce(status_ts, 0), ?),
                      delivered_em = case when ? = 'delivered' then coalesce(delivered_em, ?) else delivered_em end,
                      read_em      = case when ? = 'read'      then coalesce(read_em, ?)      else read_em end
                    where whatsapp_message_id = ?
                      and direcao = 'OUT'
                """;

        jdbc.update(sql,
                status, status,
                status,
                statusTs,
                status, statusEm,
                status, statusEm,
                wamid
        );

    }

    @Override
    public Long salvarSaidaMedia(long conversaId, String wamid, String phoneNumberIdDestino,
                                 String tipo, String mediaId, OffsetDateTime enviadoEm, String filename) {

        String sql = """
                    insert into mensagens (
                        conversa_id,
                        whatsapp_message_id,
                        direcao,
                        whatsapp_id_origem,
                        phone_number_id_destino,
                        tipo,
                        media_id,
                        nome_arquivo,
                        enviado_em,
                        status
                    )
                    values (?, ?, 'OUT', null, ?, ?, ?, ?, ?, 'sent')
                    on conflict (whatsapp_message_id)
                    do nothing
                    returning id
                """;

        return jdbc.query(sql, rs -> {
                    if (rs.next()) return rs.getLong("id");
                    return null;
                },
                conversaId,
                wamid,
                phoneNumberIdDestino,
                tipo,
                mediaId,
                filename,
                enviadoEm
        );

    }
}