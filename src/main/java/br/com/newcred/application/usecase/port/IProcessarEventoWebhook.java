package br.com.newcred.application.usecase.port;

import br.com.newcred.domain.model.EventoWebhook;

public interface IProcessarEventoWebhook {

    void executar(EventoWebhook evento);

}
