package com.example.bankcards.util;

import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;

@Component
public class CardEncryptionUtil {

    private static final String ALGORITHM = "AES";

    private final String secretKey;

    public CardEncryptionUtil(@Value("${app.card-encryption-key}") String secretKey) {
        if (secretKey == null || secretKey.length() != 32) {
            throw new IllegalStateException("Property 'app.card-encryption-key' must be exactly 32 characters (256-bit AES key)");
        }
        this.secretKey = secretKey;
    }

    private SecretKeySpec getKey() {
        return new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
    }

    public String encrypt(String cardNumber) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Card number encrypted error!", e);
        }
    }

    public String decrypt(String encryptedCardNumber) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Card number decrypted error", e);
        }
    }
}
