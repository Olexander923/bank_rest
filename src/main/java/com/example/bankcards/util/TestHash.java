package com.example.bankcards.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        String password = "admin";
        String hash = new BCryptPasswordEncoder().encode(password);
        System.out.println("New hash for '" + password + "': " + hash);
    }
}
