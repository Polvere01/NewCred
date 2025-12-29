package br.com.newcred.domain.model;

import java.time.OffsetDateTime;

public class EventoWebhook {
    private final Long id;
    private final String tipoObjeto;
    private final String payload;
    private final OffsetDateTime recebidoEm;

    private EventoWebhook(Long id, String tipoObjeto, String payload, OffsetDateTime  recebidoEm) {
        this.id = id;
        this.tipoObjeto = tipoObjeto;
        this.payload = payload;
        this.recebidoEm = recebidoEm;
    }

    public static EventoWebhook novo(String tipoObjeto, String payload) {
        return new EventoWebhook(null, tipoObjeto, payload, null);
    }

    public static EventoWebhook existente(Long id, String tipoObjeto, String payload, OffsetDateTime recebidoEm) {
        return new EventoWebhook(id, tipoObjeto, payload, recebidoEm);
    }

    public Long getId() { return id; }
    public String getTipoObjeto() { return tipoObjeto; }
    public String getPayload() { return payload; }
    public OffsetDateTime getRecebidoEm() { return recebidoEm; }
}
