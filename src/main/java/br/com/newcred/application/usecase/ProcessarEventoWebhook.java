package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.dto.WebhookDtos;
import br.com.newcred.application.usecase.port.IContatoRepository;
import br.com.newcred.application.usecase.port.IConversaRepository;
import br.com.newcred.application.usecase.port.IMensagemRepository;
import br.com.newcred.application.usecase.port.IProcessarEventoWebhook;
import br.com.newcred.domain.model.EventoWebhook;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import static br.com.newcred.application.usecase.dto.WebhookDtos.*;

@Service
public class ProcessarEventoWebhook implements IProcessarEventoWebhook {

    private final static String TEXTO = "text";
    private final static String AUDIO = "audio";
    private final static String IMAGE = "image";
    private final static String DOCUMENT = "document";
    private final static String VIDEO = "video";

    private final IContatoRepository contatoRepo;
    private final IConversaRepository conversaRepo;
    private final IMensagemRepository mensagemRepo;
    private final ObjectMapper mapper;

    public ProcessarEventoWebhook(IContatoRepository contatoRepo, IConversaRepository conversaRepo, IMensagemRepository mensagemRepo, ObjectMapper mapper) {
        this.contatoRepo = contatoRepo;
        this.conversaRepo = conversaRepo;
        this.mensagemRepo = mensagemRepo;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public void executar(EventoWebhook evento) {
        try {
            WebhookPayloadDTO payload = mapper.readValue(evento.getPayload(), WebhookDtos.WebhookPayloadDTO.class);

            if (payload.entry() == null || payload.entry().isEmpty()) return;

            payload.entry().stream()
                    .filter(e -> e.changes() != null)
                    .flatMap(e -> e.changes().stream())
                    .map(ChangeDTO::value)
                    .filter(Objects::nonNull)
                    .forEach(this::processarValue);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar evento webhook", e);
        }
    }

    private void processarValue(ValueDTO value) {
        var contact = primeiroContato(value);
        if (contact == null) return;

        String waId = contact.wa_id();
        if (waId == null || waId.isBlank()) return;

        String phoneNumberId = (value.metadata() != null) ? value.metadata().phone_number_id() : null;
        if (phoneNumberId == null || phoneNumberId.isBlank()) return;

        String nome = (contact.profile() != null) ? contact.profile().name() : null;

        // 1) UPSERT contato
        long contatoId = contatoRepo.upsertERetornarId(waId, nome);

        // 2) conversa: timestamp da primeira message (se existir)
        var msg = primeiraMessage(value);
        OffsetDateTime ultimaMsgEm = (msg != null) ? parseEpoch(msg.timestamp()) : OffsetDateTime.now(ZoneOffset.UTC);

        // 3) UPSERT conversa e pega id (aqui vale retornar porque vamos usar no save da mensagem IN)
        long conversaId = conversaRepo.criarOuAtualizar(contatoId, ultimaMsgEm, phoneNumberId);

        // 4) Atribui conversa ao operador
        conversaRepo.atribuirOperadorSeNulo(conversaId, phoneNumberId);

        // 4) salva mensagem IN (se tiver message)
        if (msg != null) {
            salvarMensagemEntrada(conversaId, msg, ultimaMsgEm);
        }
    }

    private void salvarMensagemEntrada(long conversaId, MessageDTO msg, OffsetDateTime enviadoEm) {
        String wamid = msg.id();
        if (wamid == null || wamid.isBlank()) return;

        String from = msg.from();
        String texto = (msg.text() != null) ? msg.text().body() : null;

        long ts = 0L;
        if (msg.timestamp() != null && !msg.timestamp().isBlank()) {
            ts = Long.parseLong(msg.timestamp());
        }
        //TODO criar um estrategy
        if (TEXTO.equals(msg.type())) {
            mensagemRepo.salvarEntrada(conversaId, wamid, from, texto, ts, enviadoEm);
        }

        if (AUDIO.equals(msg.type())) {
            mensagemRepo.salvarEntradaMedia(
                    conversaId,
                    wamid,
                    from,
                    AUDIO,
                    msg.audio().id(),
                    ts,
                    enviadoEm,
                    null
            );
        }

        if (IMAGE.equals(msg.type())) {
            String mediaId = msg.image().id(); // <-- aqui
            mensagemRepo.salvarEntradaMedia(
                    conversaId,
                    msg.id(),          // wamid
                    msg.from(),
                    "image",
                    mediaId,
                    Long.parseLong(msg.timestamp()),
                    parseEpoch(msg.timestamp()),
                    null
            );
        }
        if (DOCUMENT.equals(msg.type())) {
            String mediaId = msg.document().id(); // <-- aqui
            mensagemRepo.salvarEntradaMedia(
                    conversaId,
                    msg.id(),          // wamid
                    msg.from(),
                    "document",
                    mediaId,
                    Long.parseLong(msg.timestamp()),
                    parseEpoch(msg.timestamp()),
                    msg.document().filename()
            );
        }

        if (VIDEO.equals(msg.type())) {
            String mediaId = msg.video().id(); // <-- aqui
            mensagemRepo.salvarEntradaMedia(
                    conversaId,
                    msg.id(),          // wamid
                    msg.from(),
                    "video",
                    mediaId,
                    Long.parseLong(msg.timestamp()),
                    parseEpoch(msg.timestamp()),
                    null
            );
        }

    }

    private static ContactDTO primeiroContato(ValueDTO value) {
        if (value.contacts() == null || value.contacts().isEmpty()) return null;
        return value.contacts().get(0);
    }

    private static MessageDTO primeiraMessage(ValueDTO value) {
        if (value.messages() == null || value.messages().isEmpty()) return null;
        return value.messages().get(0);
    }

    private static OffsetDateTime parseEpoch(String epochStr) {
        if (epochStr == null || epochStr.isBlank()) {
            return OffsetDateTime.now(ZoneOffset.UTC);
        }
        long epoch = Long.parseLong(epochStr); // WhatsApp manda em segundos
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
    }
}