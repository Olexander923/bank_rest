package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.UserNameAlreadyExistException;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * для ADMIN (управление пользователями)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserService userService;


    @PostMapping
    public ResponseEntity<UserResponseDTO> createUsers(@Valid
            @RequestBody UserCreateRequestDTO createRequest) {
        User user = userService.createUser(
                createRequest.getUsername(),
                createRequest.getPassword(),
                createRequest.getEmail(),
                createRequest.getRole()
        );
        UserResponseDTO userResponse = UserResponseDTO.fromEntity(user);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUsers(@Valid
            @RequestBody UserUpdateRequestDTO updateRequest) throws UserNameAlreadyExistException {
        User user = userService.updateUser(
                updateRequest.getUserId(),
                updateRequest.getUsername(),
                updateRequest.getEmail()
        );
        UserResponseDTO userResponse = UserResponseDTO.fromEntity(user);
        return ResponseEntity.ok(userResponse);
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUsers(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}
