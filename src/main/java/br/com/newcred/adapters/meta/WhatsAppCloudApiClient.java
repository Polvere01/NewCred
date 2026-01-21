package br.com.newcred.adapters.meta;

import br.com.newcred.application.usecase.port.IMetaTokenProvider;
import br.com.newcred.application.usecase.port.IWhatsAppCloudApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class WhatsAppCloudApiClient implements IWhatsAppCloudApiClient {

    //TODO QUEBRAR EM CLASSES MENORES
    private final RestClient rest;
    private final String baseUrl;
    private final ObjectMapper mapper;
    private final IMetaTokenProvider iMetaTokenProvider;

    public WhatsAppCloudApiClient(
            RestClient.Builder builder,
            ObjectMapper mapper,
            @Value("${meta.base-url:https://graph.facebook.com}") String baseUrl,
            @Value("${meta.version}") String version,
            IMetaTokenProvider iMetaTokenProvider
    ) {
        this.mapper = mapper;
        this.baseUrl = baseUrl + "/" + version;
        this.iMetaTokenProvider = iMetaTokenProvider;
        this.rest = builder.build();
    }


    @Override
    public String enviarTexto(String phoneNumberId, String to, String body) {
        var url = baseUrl + "/" + phoneNumberId + "/messages";

        var payload = new SendTextRequest(
                "whatsapp",
                "individual",
                to,
                "text",
                new Text(false, body)
        );

        String raw = postWithAuth(phoneNumberId, url)
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
    ) {
    }

    public String uploadMedia(String phoneNumberId, byte[] bytes, String mimeType, String filename) {
        String url = baseUrl + "/" + phoneNumberId + "/media";

        var form = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        form.add("messaging_product", "whatsapp");
        form.add("type", mimeType); // "audio/ogg"

        var resource = new org.springframework.core.io.ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename; // "audio.ogg"
            }
        };

        var fileHeaders = new org.springframework.http.HttpHeaders();
        fileHeaders.setContentType(org.springframework.http.MediaType.parseMediaType(mimeType));
        fileHeaders.setContentDispositionFormData("file", filename);

        var filePart = new org.springframework.http.HttpEntity<>(resource, fileHeaders);
        form.add("file", filePart);

        String raw = postWithAuth(phoneNumberId, url)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(String.class);

        try {
            // resposta do upload é tipo { "id": "...." }
            var node = mapper.readTree(raw);
            var id = node.path("id").asText(null);
            if (id == null || id.isBlank()) {
                throw new RuntimeException("Meta não retornou media id. Resposta=" + raw);
            }
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Erro lendo resposta do upload. Resposta=" + raw, e);
        }
    }


    public record UploadMediaResponse(String id) {
    }

    @Override
    public String enviarAudioPorMediaId(String phoneNumberId, String to, String mediaId) {
        String url = baseUrl + "/" + phoneNumberId + "/messages";

        var payload = new SendAudioRequest(
                "whatsapp",
                "individual",
                to,
                "audio",
                new Audio(mediaId)
        );

        String raw = postWithAuth(phoneNumberId, url)
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

    @Override
    public String enviarVideoPorMediaId(String phoneNumberId, String to, String mediaId) {
        String url = baseUrl + "/" + phoneNumberId + "/messages";

        var payload = new SendVideoRequest(
                "whatsapp",
                "individual",
                to,
                "video",
                new Video(mediaId)
        );

        String raw = postWithAuth(phoneNumberId, url)
                .accept(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);

        try {
            SendTextResponse resp = mapper.readValue(raw, SendTextResponse.class);
            if (resp.messages() == null || resp.messages().isEmpty() || resp.messages().get(0).id() == null) {
                throw new RuntimeException("Meta não retornou wamid. Resposta=" + raw);
            }

            //todo verificas get0
            return resp.messages().get(0).id();
        } catch (Exception e) {
            throw new RuntimeException("Erro lendo resposta do send video. Resposta=" + raw, e);
        }
    }

    public record SendVideoRequest(
            String messaging_product,
            String recipient_type,
            String to,
            String type,
            Video video
    ) {
    }


    public record SendAudioRequest(
            String messaging_product,
            String recipient_type,
            String to,
            String type,
            Audio audio
    ) {
    }

    @Override
    public String enviarImagemPorMediaId(String phoneNumberId, String to, String mediaId, String caption) {
        String url = baseUrl + "/" + phoneNumberId + "/messages";

        var image = (caption == null || caption.isBlank())
                ? new Image(mediaId, null)
                : new Image(mediaId, caption);

        var payload = new SendImageRequest(
                "whatsapp",
                "individual",
                to,
                "image",
                image
        );

        String raw = postWithAuth(phoneNumberId, url)
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
            throw new RuntimeException("Erro lendo resposta do send image. Resposta=" + raw, e);
        }
    }

    public record SendImageRequest(
            String messaging_product,
            String recipient_type,
            String to,
            String type,
            Image image
    ) {
    }

    @Override
    public String enviarDocumentoPorMediaId(String phoneNumberId, String to, String mediaId, String filename, String caption) {
        String url = baseUrl + "/" + phoneNumberId + "/messages";

        var doc = new Document(mediaId, filename, (caption == null || caption.isBlank()) ? null : caption);

        var payload = new SendDocumentRequest(
                "whatsapp",
                "individual",
                to,
                "document",
                doc
        );

        String raw = postWithAuth(phoneNumberId, url)
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
            throw new RuntimeException("Erro lendo resposta do send document. Resposta=" + raw, e);
        }
    }

    @Override
    public String getPhoneNumberId() {
        return "";
    }

    private RestClient.RequestBodySpec postWithAuth(String phoneNumberId, String url) {
        String token = iMetaTokenProvider.getToken(phoneNumberId);
        return rest.post()
                .uri(url)
                .header("Authorization", "Bearer " + token);
    }

    public record SendDocumentRequest(
            String messaging_product,
            String recipient_type,
            String to,
            String type,
            Document document
    ) {
    }

    public record Document(String id, String filename, String caption) {
    }

    public record Image(String id, String caption) {
    }

    public record Audio(String id) {
    }

    public record Video(String id) {
    }

    public record Text(boolean preview_url, String body) {
    }

    // ✅ AGORA É RECORD: resp.messages() existe
    public record SendTextResponse(List<MessageId> messages) {
    }

    public record MessageId(String id) {
    }
}