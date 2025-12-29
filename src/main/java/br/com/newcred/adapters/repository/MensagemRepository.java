package br.com.newcred.adapters.repository;

import br.com.newcred.application.usecase.port.IMensagemRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

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
                enviado_em
            )
            values (?, ?, 'OUT', null, ?, 'text', ?, now())
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
}