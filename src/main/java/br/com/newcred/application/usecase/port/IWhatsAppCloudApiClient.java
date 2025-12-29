package br.com.newcred.application.usecase.port;

public interface IWhatsAppCloudApiClient {
    String enviarTexto(String to, String body);
}
