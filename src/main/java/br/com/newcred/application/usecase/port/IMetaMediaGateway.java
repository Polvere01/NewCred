package br.com.newcred.application.usecase.port;

import java.io.InputStream;

public interface IMetaMediaGateway {
    MetaMediaInfo obterMediaInfo(String mediaId, String phoneNumberId); // retorna url + mime
    InputStream baixarMediaStream(String mediaUrl, String phoneNumberId);

    record MetaMediaInfo(String url, String mimeType) {}
}
