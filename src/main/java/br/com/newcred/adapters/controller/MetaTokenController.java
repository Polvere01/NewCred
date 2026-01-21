package br.com.newcred.adapters.controller;

import br.com.newcred.adapters.config.TokenCrypto;
import br.com.newcred.application.usecase.port.IPhoneNumberMetaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/meta-token")
public class MetaTokenController {

    private final IPhoneNumberMetaRepository repo;
    private final TokenCrypto crypto;

    public MetaTokenController(
            IPhoneNumberMetaRepository repo,
            TokenCrypto crypto
    ) {
        this.repo = repo;
        this.crypto = crypto;
    }

    @PostMapping
    public ResponseEntity<Void> salvar(
            @RequestParam String phoneNumberId,
            @RequestParam String token
    ) {
        var enc = crypto.encrypt(token);

        repo.upsert(
                phoneNumberId,
                enc.encB64(),
                enc.nonceB64(),
                true
        );

        return ResponseEntity.ok().build();
    }
}
