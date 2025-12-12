package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.dto.UserResponseDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/auth/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
            //создание аутентификации
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            System.out.println("Authentication successful!");

            SecurityContextHolder.getContext().setAuthentication(authentication);//теперь сохраняем
            //генерируем и возвращаем
            System.out.println("Auth successful for: " + request.getUsername());
            String jwt = jwtUtils.tokenGeneration((UserDetails) authentication.getPrincipal());

            return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println("Received email: " + request.getEmail());
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new com.example.bankcards.exception.ValidationException("Email is required!");
        }
        //проверяем пользователя по username и email, создаем нового
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("User name is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("This email already exist!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));//шифр пароля
        user.setEmail(request.getEmail());

        Role userRole = Role.USER;
        user.setRole(userRole);
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(savedUser));
    }
}
