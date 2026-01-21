package br.com.newcred.application.usecase.port;

public interface IMetaTokenProvider {
    String getToken(String phoneNumberId);
}
