package br.com.newcred.application.usecase.port;

public interface IngestWebhook {
    void executar(String payload);
}
