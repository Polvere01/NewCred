package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.port.IEventoWebhookRepository;
import br.com.newcred.domain.model.EventoWebhook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class IIngestWebhook implements br.com.newcred.application.usecase.port.IIngestWebhook {

    private final IEventoWebhookRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public IIngestWebhook(IEventoWebhookRepository repo) {
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
