package br.com.newcred.application.usecase.dto;

public record CriarOperadorRequestDto(
        String nome,
        String email,
        String senha,
        String role,
        Long supervisorId
) {}