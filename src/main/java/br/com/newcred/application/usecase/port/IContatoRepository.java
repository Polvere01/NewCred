package br.com.newcred.application.usecase.port;

import br.com.newcred.domain.model.Contato;

import java.util.Optional;

public interface IContatoRepository {
    long upsertERetornarId(String whatsappId, String nome);
}
