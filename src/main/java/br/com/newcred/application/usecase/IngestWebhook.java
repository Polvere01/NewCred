package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.port.EventoWebhookRepository;
import br.com.newcred.domain.model.EventoWebhook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class IngestWebhook implements br.com.newcred.application.usecase.port.IngestWebhook {

    private final EventoWebhookRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public IngestWebhook(EventoWebhookRepository repo) {
        this.repo = repo;
    }

    @Override
    public void executar(String payload) {
        String tipoObjeto = extrairTipoObjeto(payload);
        EventoWebhook evento = EventoWebhook.novo(tipoObjeto, payload);
        repo.salvar(evento);
    }

    private String extrairTipoObjeto(String payload) {
        try {
            JsonNode root = mapper.readTree(payload);
            JsonNode obj = root.get("object");
            return obj != null ? obj.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
