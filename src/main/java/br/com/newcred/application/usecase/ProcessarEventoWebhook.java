package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.dto.WebhookDtos;
import br.com.newcred.application.usecase.port.IContatoRepository;
import br.com.newcred.application.usecase.port.IConversaRepository;
import br.com.newcred.application.usecase.port.IProcessarEventoWebhook;
import br.com.newcred.domain.model.Contato;
import br.com.newcred.domain.model.EventoWebhook;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import br.com.newcred.application.usecase.dto.WebhookDtos.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Service
public class ProcessarEventoWebhook implements IProcessarEventoWebhook {

    private final IContatoRepository iContatoRepository;
    private final IConversaRepository iConversaRepository;
    private final ObjectMapper mapper;

    public ProcessarEventoWebhook(IContatoRepository iContatoRepository,
                                  IConversaRepository iConversaRepository,
                                  ObjectMapper mapper) {
        this.iContatoRepository = iContatoRepository;
        this.iConversaRepository = iConversaRepository;
        this.mapper = mapper;
    }

    @Override
    public void executar(EventoWebhook evento) {
        try {
            WebhookPayloadDTO payload =
                    mapper.readValue(evento.getPayload(), WebhookDtos.WebhookPayloadDTO.class);

            if (payload.entry() == null) return;

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
        // 1) contato (pega do primeiro contact)
        if (value.contacts() == null || value.contacts().isEmpty()) return;

        ContactDTO contact = value.contacts().get(0);
        String waId = contact.wa_id();
        if (waId == null || waId.isBlank()) return;

        String nome = (contact.profile() != null) ? contact.profile().name() : null;

        long contatoId = salvarOuAtualizarContatoRetornandoId(waId, nome);

        // 2) conversa (usa timestamp da primeira message, se tiver)
        String ts = null;
        if (value.messages() != null && !value.messages().isEmpty()) {
            ts = value.messages().get(0).timestamp();
        }
        OffsetDateTime ultimaMsgEm = parseEpoch(ts);

        salvarOuAtualizarConversa(contatoId, ultimaMsgEm);
    }

    private long salvarOuAtualizarContatoRetornandoId(String waId, String nome) {
        return iContatoRepository.upsertERetornarId(waId, nome);
    }


    private long salvarOuAtualizarConversa(long contatoId, OffsetDateTime ultimaMensagemEm) {
        return iConversaRepository.criarOuAtualizar(contatoId, ultimaMensagemEm);
    }

    private static OffsetDateTime parseEpoch(String epochStr) {
        if (epochStr == null || epochStr.isBlank()) {
            return OffsetDateTime.now(ZoneOffset.UTC);
        }
        long epoch = Long.parseLong(epochStr); // WhatsApp manda em segundos
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
    }
}