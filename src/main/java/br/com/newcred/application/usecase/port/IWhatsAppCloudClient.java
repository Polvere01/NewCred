package br.com.newcred.application.usecase.port;

public interface IWhatsAppCloudClient {
    void enviarTemplate(String template, String telefone, String nome, String phoneNumberId);
}
