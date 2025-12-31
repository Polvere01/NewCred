package br.com.newcred.adapters.meta;

import br.com.newcred.adapters.dto.RetrieveMediaResponseDto;
import br.com.newcred.application.usecase.port.IMetaMediaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.InputStream;

@Component
public class WhatsAppCloudApiClientMediaDownload implements IMetaMediaGateway {

    private final RestClient rest;
    private final String baseUrl;
    private final String phoneNumberId;

    public WhatsAppCloudApiClientMediaDownload(
            RestClient.Builder builder,
            @Value("${meta.base-url:https://graph.facebook.com}") String baseUrl,
            @Value("${meta.version}") String version,
            @Value("${meta.phone-number-id}") String phoneNumberId,
            @Value("${meta.token}") String token
    ) {
        this.baseUrl = baseUrl + "/" + version;
        this.phoneNumberId = phoneNumberId;
        this.rest = builder.defaultHeader("Authorization", "Bearer " + token).build();
    }

    @Override
    public MetaMediaInfo obterMediaInfo(String mediaId) {
        String url = baseUrl + "/" + mediaId + "?phone_number_id=" + phoneNumberId;

        RetrieveMediaResponseDto resp = rest.get()
                .uri(url)
                .header("Accept", "application/json")
                .retrieve()
                .body(RetrieveMediaResponseDto.class);

        if (resp == null || resp.url() == null) {
            throw new RuntimeException("Meta não retornou URL para mediaId=" + mediaId);
        }

        return new MetaMediaInfo(resp.url(), resp.mime_type());
    }

    @Override
    public InputStream baixarMediaStream(String mediaUrl) {
        var resource = rest.get()
                .uri(mediaUrl)
                .retrieve()
                .body(org.springframework.core.io.Resource.class);

        try {
            if (resource == null) throw new RuntimeException("Download da mídia retornou vazio");
            return resource.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Erro lendo stream da mídia", e);
        }
    }
}

