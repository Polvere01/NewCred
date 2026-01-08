package br.com.newcred.adapters.dto;

import java.time.OffsetDateTime;

public record MensagemDTO(
        long id,
        String texto,
        String tipo,
        String direcao,
        OffsetDateTime createdAt,
        String status,
        String phoneNumberId
) {}