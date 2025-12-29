package br.com.newcred.adapters.client;

import br.com.newcred.application.usecase.port.IWhatsAppCloudApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

    public record Text(boolean preview_url, String body) {}

    // ✅ AGORA É RECORD: resp.messages() existe
    public record SendTextResponse(List<MessageId> messages) {}

    public record MessageId(String id) {}
}