package br.com.newcred.adapter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebhookController {

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
        System.out.println("=== WEBHOOK RECEBIDO ===");
        System.out.println(body);
        System.out.println("========================");
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/")
    public String health() {
        return "ok";
    }
}