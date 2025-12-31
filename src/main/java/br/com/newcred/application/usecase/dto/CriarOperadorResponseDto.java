package br.com.newcred.application.usecase.dto;

public record CriarOperadorResponseDto(
        Long id,
        String nome,
        String email,
        String role,
        Boolean ativo
) {}