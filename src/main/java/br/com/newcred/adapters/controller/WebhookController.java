package br.com.newcred.adapters.controller;

import br.com.newcred.application.usecase.port.IngestWebhook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebhookController {

    private final IngestWebhook ingestWebhook;

    public WebhookController(IngestWebhook ingestWebhook) {
        this.ingestWebhook = ingestWebhook;
    }

    @GetMapping("/webhook")
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge
    ) {
        String verifyToken = System.getenv("VERIFY_TOKEN");

        if ("subscribe".equals(mode) && verifyToken != null && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("forbidden");
    }


    @PostMapping("/webhook")
    public ResponseEntity<String> receive(@RequestBody String body) {

        System.out.println(body);

        ingestWebhook.executar(body);
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/")
    public String health() {
        return "ok";
    }
}