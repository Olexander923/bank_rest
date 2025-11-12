package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        System.out.println(">>> Login attempt: " + request.getUsername());
        try {
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
            System.out.println(">>> JWT: " + jwt);


            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (AuthenticationException e){
            System.out.println(">>> AUTH FAILED: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            //throw e;
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication failed");
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        System.out.println("Received email: " + request.getEmail());
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required!");
        }
        //проверяем пользователя, создаем нового
       if(userRepository.existsByUsername(request.getUsername())) {
           return ResponseEntity.badRequest().body("Error: Username is already taken!");
       }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));//шифр пароля
        user.setEmail(request.getEmail());

        Role userRole = Role.USER;
        user.setRole(userRole);
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }
}
