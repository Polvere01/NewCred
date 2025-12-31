package br.com.newcred.application.usecase.dto;

public record LoginResponseDto( String token,
                                UserDto user) {
}
