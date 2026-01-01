package br.com.newcred.adapters.controller;

import br.com.newcred.application.usecase.dto.CriarOperadorRequestDto;
import br.com.newcred.application.usecase.dto.CriarOperadorResponseDto;
import br.com.newcred.application.usecase.port.ICriarOperador;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operadores")
public class OperadoresController {

    private final ICriarOperador criarOperador;

    public OperadoresController(ICriarOperador criarOperador) {
        this.criarOperador = criarOperador;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CriarOperadorResponseDto> criar(@RequestBody CriarOperadorRequestDto dto) {
        var resp = criarOperador.executar(dto);
        return ResponseEntity.ok(resp);
    }
}