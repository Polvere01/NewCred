package br.com.newcred.application.usecase.port;

import org.springframework.web.multipart.MultipartFile;

public interface IWhatsAppCloudApiClient {
    String enviarTexto(String phoneNumberId, String to, String body);
    String uploadMedia(String phoneNumbeId, byte[] bytes, String mimeType, String filename);
    String enviarAudioPorMediaId(String phoneNumberId, String to, String mediaId);
    String enviarVideoPorMediaId(String phoneNumberId, String to, String mediaId);
    String enviarImagemPorMediaId(String phoneNumberId, String to, String mediaId, String caption);
    String enviarDocumentoPorMediaId(String phoneNumberId, String to, String mediaId, String filename, String caption);
    String getPhoneNumberId();
}
