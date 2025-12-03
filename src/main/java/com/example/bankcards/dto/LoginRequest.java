package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotNull
    @Size(min = 8,max = 50,
            message = "The user name must be exactly min 8 characters long!")
    private String username;

    @NotNull
    @Size(min = 8,max = 16,
            message = "The password must be exactly min 8 characters long!")
    private String password;
}
