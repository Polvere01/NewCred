package br.com.newcred.application.usecase.port;

public interface IPhoneNumberMetaRepository {
    TokenData buscarAtivo(String phoneNumberId);

    void upsert(String phoneNumberId, String encB64, String nonceB64, boolean active);

    record TokenData(String encB64, String nonceB64) {}
}
