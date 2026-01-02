package br.com.newcred.application.usecase.port;

public interface IWhatsAppCloudClient {
    void enviarTemplate(String templateName, String to, String firstName);
}
