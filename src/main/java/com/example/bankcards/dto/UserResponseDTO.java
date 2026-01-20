package com.example.bankcards.dto;

import com.example.bankcards.constants.Role;
import com.example.bankcards.entity.User;
import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;

    public static UserResponseDTO fromEntity(User user){
            UserResponseDTO dto = new UserResponseDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole());
            return dto;
    }
}
