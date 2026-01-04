package br.com.newcred.adapters.dto;

public record ConversaListaDTO(long id,
                               String nome,
                               String ultimaMensagem,
                               String operadorNome,
                               String ultimaDirecao,
                               String ultimaTipo
) {
}
