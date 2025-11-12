package com.example.bankcards.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password; // пароль будет зашифрован перед сохранением
    private String email;
}
