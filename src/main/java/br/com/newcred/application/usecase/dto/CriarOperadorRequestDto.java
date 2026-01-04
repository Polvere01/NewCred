package br.com.newcred.application.usecase.dto;

import java.util.List;

public record CriarOperadorRequestDto(
        String nome,
        String email,
        String senha,
        String role,
        Long supervisorId,
        List<String> phoneNumberIds
) {}