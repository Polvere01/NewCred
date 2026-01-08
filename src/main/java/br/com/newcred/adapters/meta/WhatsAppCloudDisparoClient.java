package br.com.newcred.adapters.meta;

import br.com.newcred.application.usecase.port.IWhatsAppCloudClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class WhatsAppCloudDisparoClient implements IWhatsAppCloudClient {

    private final RestTemplate rest;
    private final String token;
    private final String version;

    public WhatsAppCloudDisparoClient(
            RestTemplateBuilder builder,
            @Value("${meta.token}") String token,
            @Value("${meta.version}") String version
    ) {
        this.rest = builder.build();
        this.token = token;
        this.version = version;
    }

    @Override
    public void enviarTemplate(
            String templateName,
            String to,
            String firstName,
            String cpf,
            String valorContrato,
            String phoneNumberId
    ) {
        String url = "https://graph.facebook.com/" + version + "/" + phoneNumberId + "/messages";

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", to,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", "pt_BR"),
                        "components", List.of(
                                Map.of(
                                        "type", "body",
                                        "parameters", List.of(
                                                Map.of("type", "text", "text", firstName),
                                               // Map.of("type", "text", "text", formatarCpf(cpf)),
                                                Map.of("type", "text", "text", valorContrato)
                                        )
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var req = new HttpEntity<>(body, headers);

        var resp = rest.postForEntity(url, req, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Meta retornou " + resp.getStatusCode() + ": " + resp.getBody());
        }
    }


    public void enviarTemplate(String templateName, String to, String firstName, String phoneNumberId) {
        String url = "https://graph.facebook.com/" + version + "/" + phoneNumberId + "/messages";

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", to,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", "pt_BR"),
                        "components", List.of(
                                Map.of(
                                        "type", "body",
                                        "parameters", List.of(
                                                Map.of("type", "text", "text", firstName)
                                        )
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var req = new HttpEntity<>(body, headers);

        var resp = rest.postForEntity(url, req, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Meta retornou " + resp.getStatusCode() + ": " + resp.getBody());
        }
    }

    private static String formatarCpf(String cpfRaw) {
        if (cpfRaw == null) return null;

        String digits = cpfRaw.replaceAll("\\D", "");
        if (digits.length() != 11) return cpfRaw; // fallback seguro

        return digits.substring(0, 3) + "." +
                digits.substring(3, 6) + "." +
                digits.substring(6, 9) + "-" +
                digits.substring(9);
    }
}
