package br.com.newcred.adapters.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class TokenCrypto {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int NONCE_SIZE = 12; // recomendado para GCM
    private static final int TAG_BITS = 128;

    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    public TokenCrypto(@Value("${meta.secret}") String secretHex) {
        byte[] keyBytes = HexFormat.of().parseHex(secretHex.trim());
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("META_TOKEN_SECRET deve ter 32 bytes (64 hex chars).");
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    public EncryptedToken encrypt(String plain) {
        try {
            byte[] nonce = new byte[NONCE_SIZE];
            random.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));

            byte[] cipherBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            return new EncryptedToken(
                    Base64.getEncoder().encodeToString(cipherBytes),
                    Base64.getEncoder().encodeToString(nonce)
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar token", e);
        }
    }

    public String decrypt(String encB64, String nonceB64) {
        try {
            byte[] enc = Base64.getDecoder().decode(encB64);
            byte[] nonce = Base64.getDecoder().decode(nonceB64);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));

            byte[] plain = cipher.doFinal(enc);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar token", e);
        }
    }

    public record EncryptedToken(String encB64, String nonceB64) {}
}