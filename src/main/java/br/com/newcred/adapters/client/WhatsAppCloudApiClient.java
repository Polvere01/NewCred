package br.com.newcred.adapters.client;

import br.com.newcred.application.usecase.port.IWhatsAppCloudApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class WhatsAppCloudApiClient implements IWhatsAppCloudApiClient {

    private final RestClient rest;
    private final String baseUrl;
    private final String phoneNumberId;
    private final ObjectMapper mapper;

    public WhatsAppCloudApiClient(
            RestClient.Builder builder,
            ObjectMapper mapper,
            @Value("${meta.base-url:https://graph.facebook.com}") String baseUrl,
            @Value("${meta.version}") String version,
            @Value("${meta.phone-number-id}") String phoneNumberId,
            @Value("${meta.token}") String token
    ) {
        this.mapper = mapper;
        this.baseUrl = baseUrl + "/" + version;
        this.phoneNumberId = phoneNumberId;

        this.rest = builder
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    @Override
    public String enviarTexto(String to, String body) {
        var url = baseUrl + "/" + phoneNumberId + "/messages";

        var payload = new SendTextRequest(
                "whatsapp",
                "individual",
                to,
                "text",
                new Text(false, body)
        );

        String raw = rest.post()
                .uri(url)
                .body(payload)
                .retrieve()
                .body(String.class);

        try {
            SendTextResponse resp = mapper.readValue(raw, SendTextResponse.class);

            if (resp.messages() == null || resp.messages().isEmpty() || resp.messages().get(0).id() == null) {
                throw new RuntimeException("Meta não retornou wamid. Resposta=" + raw);
            }

            return resp.messages().getFirst().id();
        } catch (Exception e) {
            throw new RuntimeException("Erro lendo resposta da Meta. Resposta=" + raw, e);
        }
    }

    public record SendTextRequest(
            String messaging_product,
            String recipient_type,
            String to,
            String type,
            Text text
    ) {}

    @Override
    public String getPhoneNumberId() {
        return phoneNumberId;
    }

    public String uploadMedia(byte[] bytes, String mimeType, String filename) {
        var url = baseUrl + "/" + phoneNumberId + "/media";

        var form = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        form.add("messaging_product", "whatsapp");
        form.add("type", mimeType);

        var resource = new org.springframework.core.io.ByteArrayResource(bytes) {
            @Override public String getFilename() { return filename; }
        };

        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType(mimeType));

        form.add("file", new org.springframework.http.HttpEntity<>(resource, headers));

        String raw = rest.post()
                .uri(url)
                .contentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA)
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(String.class);

        // parseia raw e retorna mediaId (igual você fez no send)
        try {
            var resp = mapper.readTree(raw);
            String id = resp.path("id").asText(null);
            if (id == null) throw new RuntimeException("Meta não retornou media id. raw=" + raw);
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Erro lendo resposta upload media. raw=" + raw, e);
        }
    }


    public record UploadMediaResponse(String id) {}

    @Override
    public String enviarAudioPorMediaId(String to, String mediaId) {
        String url = baseUrl + "/" + phoneNumberId + "/messages";

        var payload = new SendAudioRequest(
                "whatsapp",
                "individual",
                to,
                "audio",
                new Audio(mediaId)
        );

        String raw = rest.post()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);

        try {
            SendTextResponse resp = mapper.readValue(raw, SendTextResponse.class);
            if (resp.messages() == null || resp.messages().isEmpty() || resp.messages().get(0).id() == null) {
                throw new RuntimeException("Meta não retornou wamid. Resposta=" + raw);
            }
            return resp.messages().get(0).id();
        } catch (Exception e) {
            throw new RuntimeException("Erro lendo resposta do send audio. Resposta=" + raw, e);
        }
    }


    private HttpHeaders fileHeaders(MultipartFile file) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType(
                file.getContentType() != null ? file.getContentType() : "audio/ogg"
        ));
        return h;
    }

    public record SendAudioRequest(
            String messaging_product,
            String recipient_type,
            String to,
            String type,
            Audio audio
    ) {}
    public record Audio(String id) {}

    public record Text(boolean preview_url, String body) {}

    // ✅ AGORA É RECORD: resp.messages() existe
    public record SendTextResponse(List<MessageId> messages) {}

    public record MessageId(String id) {}
}