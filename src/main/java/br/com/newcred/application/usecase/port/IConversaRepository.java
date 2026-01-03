package br.com.newcred.application.usecase.port;

import java.time.OffsetDateTime;

public interface IConversaRepository {
    long criarOuAtualizar(long contatoId, OffsetDateTime ultimaMensagemEm ,String phoneNumberId);
    void atribuirOperadorSeNulo(long conversaId, String phoneNumberId);
}
