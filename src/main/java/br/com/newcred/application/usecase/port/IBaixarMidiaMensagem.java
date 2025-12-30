package br.com.newcred.application.usecase.port;

import br.com.newcred.application.usecase.dto.ResultadoMidiaDto;

import java.io.InputStream;

public interface IBaixarMidiaMensagem {
    ResultadoMidiaDto executar(long mensagemId);

}
