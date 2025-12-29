package br.com.newcred.application.usecase.port;

import br.com.newcred.domain.model.EventoWebhook;

import java.util.List;

public interface IEventoWebhookRepository {
    long salvar(EventoWebhook evento);
    List<EventoWebhook> buscarPendentesParaProcessar(int limite); //evita duplicidade
    void marcarProcessado(long id);
    void marcarErro(long id, String erro);
}
