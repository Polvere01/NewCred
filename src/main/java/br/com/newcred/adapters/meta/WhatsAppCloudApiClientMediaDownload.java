package br.com.newcred.adapters.meta;

import br.com.newcred.adapters.dto.RetrieveMediaResponseDto;
import br.com.newcred.application.usecase.port.IMetaMediaGateway;
import br.com.newcred.application.usecase.port.IMetaTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.InputStream;

@Component
public class WhatsAppCloudApiClientMediaDownload implements IMetaMediaGateway {

    private final RestClient rest;
    private final String baseUrl;
    private final IMetaTokenProvider iMetaTokenProvider;

    public WhatsAppCloudApiClientMediaDownload(
            RestClient.Builder builder,
            @Value("${meta.base-url:https://graph.facebook.com}") String baseUrl,
            @Value("${meta.version}") String version,
            IMetaTokenProvider iMetaTokenProvider
    ) {
        this.baseUrl = baseUrl + "/" + version;
        this.iMetaTokenProvider = iMetaTokenProvider;
        this.rest = builder.build();
    }

    @Override
    public MetaMediaInfo obterMediaInfo(String mediaId, String phoneNumberId) {
        String url = baseUrl + "/" + mediaId;

        RetrieveMediaResponseDto resp = getWithAuth(phoneNumberId, url)
                .retrieve()
                .body(RetrieveMediaResponseDto.class);

        if (resp == null || resp.url() == null) {
            throw new RuntimeException("Meta não retornou URL para mediaId=" + mediaId);
        }

        return new MetaMediaInfo(resp.url(), resp.mime_type());
    }

    @Override
    public InputStream baixarMediaStream(String mediaUrl, String phoneNumberId) {
        var resource = rest.get()
                .uri(mediaUrl)
                .header("Authorization", "Bearer " + iMetaTokenProvider.getToken(phoneNumberId))
                .retrieve()
                .body(org.springframework.core.io.Resource.class);

        try {
            if (resource == null) throw new RuntimeException("Download da mídia retornou vazio");
            return resource.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Erro lendo stream da mídia", e);
        }
    }


    private RestClient.RequestHeadersSpec<?> getWithAuth(String phoneNumberId, String url) {
        String token = iMetaTokenProvider.getToken(phoneNumberId);
        return rest.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json");
    }

}

