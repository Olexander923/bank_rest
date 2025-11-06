package com.example.bankcards.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Security {

    //хэширование пароля с помощью PBKDF2
    public void hashPassword(String password) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Algorithm error: " + e.getMessage());
        } catch (InvalidKeySpecException e) {
            System.err.println("Hash error: " + e.getMessage());
        }

    }
}
