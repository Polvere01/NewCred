package br.com.newcred.application.usecase.port;

public interface IIngestWebhook {
    void executar(String payload);
}
