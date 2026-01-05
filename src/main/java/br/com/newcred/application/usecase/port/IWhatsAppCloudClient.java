package br.com.newcred.application.usecase.port;

public interface IWhatsAppCloudClient {
    void enviarTemplate(String template, String telefone, String nome, String cpf, String valorContrato, String phoneNumberId);
    void enviarTemplate(String template, String telefone, String nome, String phoneNumberId);
}
