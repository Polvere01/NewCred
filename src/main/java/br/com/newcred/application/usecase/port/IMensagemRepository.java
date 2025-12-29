package br.com.newcred.application.usecase.port;

import java.time.OffsetDateTime;

public interface IMensagemRepository {

    void salvarEntrada(
            long conversaId,
            String wamid,
            String whatsappIdOrigem,
            String texto,
            long timestampWhatsapp,
            OffsetDateTime enviadoEm
    );

    void salvarSaida(Long conversaID, String phoneNumberIdDestino, String idDestino, String texto);
}
