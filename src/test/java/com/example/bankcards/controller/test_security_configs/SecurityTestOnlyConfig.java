package com.example.bankcards.controller.test_security_configs;

import com.example.bankcards.controller.AdminUserController;
import com.example.bankcards.controller.AuthController;
import com.example.bankcards.controller.UserCardController;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetailService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtUtils;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.RefreshTokenService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardEncryptionUtil;
import com.example.bankcards.util.CardMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@EnableWebSecurity
public class SecurityTestOnlyConfig {
    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") //доступно только админу
                        .anyRequest().permitAll())
                .build();
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager() {
        return mock(AuthenticationManager.class);
    }

    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(mock(JwtUtils.class), mock(CustomUserDetailService.class)) {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                //пропускаем все запросы без проверки jwt
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    @Primary
    public CardEncryptionUtil cardEncryptionUtil() {
        CardEncryptionUtil mock = mock(CardEncryptionUtil.class);
        when(mock.decrypt(anyString())).thenReturn("4000000000000002");
        return mock;
    }


    @Bean
    @Primary
    public UserCardController userCardController() {
        return new UserCardController(
                mock(CardService.class),
                mock(TransferService.class),
                mock(CardMapper.class)
        );
    }

    @Bean
    @Primary
    public AuthController authController() {
        return new AuthController(
                mock(AuthenticationManager.class),
                mock(JwtUtils.class),
                mock(UserRepository.class),
                mock(PasswordEncoder.class),
                mock(RefreshTokenService.class),
                mock(RefreshTokenRepository.class),
                mock(CustomUserDetailService.class)
        );
    }

    @Bean
    @Primary
    public AdminUserController adminUserController() {
        return new AdminUserController(mock(UserService.class));
    }
}
