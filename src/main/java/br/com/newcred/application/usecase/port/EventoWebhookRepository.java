package br.com.newcred.application.usecase.port;

import br.com.newcred.domain.model.EventoWebhook;

public interface EventoWebhookRepository {
    long salvar(EventoWebhook evento);
    void marcarProcessado(long id);
    void marcarErro(long id, String erro);
}
