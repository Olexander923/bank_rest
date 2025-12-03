package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserCreateRequestDTO {
    @NotNull
    @Size(min = 8,max = 50,
            message = "The user name must be exactly min 8 characters long!")
    @NotBlank(message = "User cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",message = "Username can only contain letters, numbers and underscores")
    private String username;

    @Pattern(
            regexp = "((?=.*[a-z])(?=.*\\d)(?=.*[@#$%])(?=.*[A-Z]).{8,16})",
            message = "Password too weak! Password must contain lowercase,uppercase, digit, special char @#$%, 8-16 chars")
    private String password;

    @NotNull
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull
    private Role role;
}
