package br.com.newcred.adapters.controller;

import br.com.newcred.application.usecase.dto.DisparoResultadoDto;
import br.com.newcred.application.usecase.port.IDispararTemplateEmMassa;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/disparos")
public class DisparoController {

    private final IDispararTemplateEmMassa disparar;

    public DisparoController(IDispararTemplateEmMassa disparar) {
        this.disparar = disparar;
    }

    @PostMapping(value="/template", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DisparoResultadoDto> dispararTemplate(
            @RequestParam("template") String template,
            @RequestPart("file") MultipartFile file
    ) {
        var resp = disparar.executar(template, file);
        return ResponseEntity.ok(resp);
    }
}