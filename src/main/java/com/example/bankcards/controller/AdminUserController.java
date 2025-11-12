package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.UserNameAlreadyExistException;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * для ADMIN (управление пользователями)
 */
@RestController
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;


    @RequestMapping("/api/admin/user")
    public ResponseEntity<UserResponseDTO> createUsers(
            @RequestBody UserCreateRequestDTO createRequest) throws UserNameAlreadyExistException, EmailAlreadyExistsException {
        User user = userService.createUser(
                createRequest.getUsername(),
                createRequest.getEmail(),
                createRequest.getPassword(),
                createRequest.getRole()
        );
        UserResponseDTO userResponse = UserResponseDTO.fromEntity(user);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/api/admin/users/{userId}")
    public ResponseEntity<UserResponseDTO> updateUsers(
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
    public ResponseEntity<Void> deleteUsers(@PathVariable Long userId){
         userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}
