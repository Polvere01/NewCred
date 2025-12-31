package br.com.newcred.application.usecase.port;

import org.springframework.web.multipart.MultipartFile;

public interface IAudioConverter {
    AudioConvertResult webmToOggOpus(MultipartFile input);

    record AudioConvertResult(byte[] bytes, String mimeType, String filename) {}
}
