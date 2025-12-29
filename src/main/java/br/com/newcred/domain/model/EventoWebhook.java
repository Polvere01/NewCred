package br.com.newcred.domain.model;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public class EventoWebhook {

    private final String tipoObjeto;
    private final String payload;
    private final ZonedDateTime recebidoEm;

    private EventoWebhook(String tipoObjeto, String payload, ZonedDateTime recebidoEm) {
        this.tipoObjeto = tipoObjeto;
        this.payload = payload;
        this.recebidoEm = recebidoEm;
    }

    public static EventoWebhook novo(String tipoObjeto, String payload) {
        return new EventoWebhook(tipoObjeto, payload, ZonedDateTime.now());
    }

    public String getTipoObjeto() { return tipoObjeto; }
    public String getPayload() { return payload; }
    public ZonedDateTime getRecebidoEm() { return recebidoEm; }
}
