package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import lombok.Data;

@Data
public class UserCreateRequestDTO {
    private String username;
    private String password;
    private String email;
    private Role role;
}
