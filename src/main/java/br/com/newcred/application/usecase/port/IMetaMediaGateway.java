package br.com.newcred.application.usecase.port;

import java.io.InputStream;

public interface IMetaMediaGateway {
    MetaMediaInfo obterMediaInfo(String mediaId); // retorna url + mime
    InputStream baixarMediaStream(String mediaUrl);

    record MetaMediaInfo(String url, String mimeType) {}
}
