package br.com.newcred.application.usecase.dto;

public record MensagensRequestDto (Long conversaId, String waIdDestino, String texto, String phoneNumberId) {}