package br.com.newcred.application.usecase.port;

import br.com.newcred.application.usecase.dto.DisparoResultadoDto;
import org.springframework.web.multipart.MultipartFile;

public interface IDispararTemplateEmMassa {

    DisparoResultadoDto executar(String template, MultipartFile file);
}
