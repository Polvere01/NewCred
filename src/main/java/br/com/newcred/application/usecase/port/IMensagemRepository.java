package br.com.newcred.application.usecase.port;

import br.com.newcred.adapters.dto.MensagemDTO;

import java.time.OffsetDateTime;
import java.util.List;

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

    List<MensagemDTO> listarPorConversa(long conversaId);
}
