package br.com.newcred.application.usecase.dto;

import java.io.InputStream;

public record ResultadoMidiaDto(String mimeType, InputStream stream) {
}
