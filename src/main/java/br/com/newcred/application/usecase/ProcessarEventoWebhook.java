package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.port.IEventoWebhookRepository;
import br.com.newcred.application.usecase.port.IProcessarEventoWebhook;
import br.com.newcred.domain.model.EventoWebhook;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class ProcessarEventoWebhook implements IProcessarEventoWebhook {
    private final IEventoWebhookRepository repo;

    public ProcessarEventoWebhook(IEventoWebhookRepository repo) {
        this.repo = repo;
    }

    public void executar(EventoWebhook evento) {
        // por enquanto só exemplo: aqui você vai parsear JSON e salvar contato/conversa/mensagem
        // depois a gente implementa o parse do payload e popular as outras tabelas.
        // Se der erro, lança exception e o scheduler marca erro.

        // Ex: validar que payload não tá vazio
        if (evento.getPayload() == null || evento.getPayload().isBlank()) {
            throw new IllegalArgumentException("Payload vazio");
        }
    }
}
