package br.com.newcred.application.usecase.port;

import org.springframework.web.multipart.MultipartFile;

public interface IWhatsAppCloudApiClient {
    String enviarTexto(String to, String body);
    String uploadMedia(byte[] bytes, String mimeType, String filename);
    String enviarAudioPorMediaId(String to, String mediaId);
    String enviarVideoPorMediaId(String to, String mediaId);
    String enviarImagemPorMediaId(String to, String mediaId, String caption);
    String enviarDocumentoPorMediaId(String to, String mediaId, String filename, String caption);
    String getPhoneNumberId();
}
