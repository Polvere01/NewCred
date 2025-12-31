package br.com.newcred.application.usecase.dto;

public record MediaConvertResult(byte[] bytes, String mimeType, String filename) {
}
