package br.com.newcred.application.usecase.port;

import br.com.newcred.adapters.dto.MensagemDTO;
import br.com.newcred.application.usecase.dto.MensagemMediaInfoDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface IMensagemRepository {

    void salvarEntrada(
            long conversaId,
            String wamid,
            String whatsappIdOrigem,
            String texto,
            long timestampWhatsapp,
            OffsetDateTime enviadoEm
    );


    void salvarEntradaMedia(
            long conversaId,
            String wamid,
            String whatsappIdOrigem,
            String tipo,
            String mediaId,
            long timestampWhatsapp,
            OffsetDateTime enviadoEm,
            String filename
    );

    void salvarSaida(Long conversaID, String phoneNumberIdDestino, String idDestino, String texto);

    Long salvarSaidaMedia(long conversaId, String wamid, String phoneNumberIdDestino,
                          String tipo, String mediaId, OffsetDateTime enviadoEm, String filename);

    List<MensagemDTO> listarPorConversa(long conversaId);

    Optional<MensagemMediaInfoDto> buscarMediaInfo(long mensagemId);
}
