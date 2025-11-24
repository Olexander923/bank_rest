package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetailService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtUtils;
import com.example.bankcards.service.CardService;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestConfiguration
@EnableWebSecurity
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CardMapper.class))
public class TestSecurityConfig {
    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
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
                // Пропускаем все запросы без проверки JWT
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    @Primary
//    public CardMapper cardMapper() {
//        CardMapper mock = mock(CardMapper.class);
//        when(mock.toDTO(any())).thenAnswer(invocation -> {
//            Card card = invocation.getArgument(0);
//            System.out.println("Card ID in mapper: " + card.getId());
//            CardResponseDTO dto = new CardResponseDTO();
//            dto.setId(card.getId()); // Используем ID из карты
//            dto.setUserId(card.getUser().getId());
//            dto.setMaskedNumber("400000******0002");
//            dto.setBalance(card.getBalance());
//            dto.setCardStatus(card.getCardStatus());
//            dto.setExpireDate(card.getExpireDate());
//            return dto;
//        });
//        return mock;
//    }

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
                mock(PasswordEncoder.class)
        );
    }

    @Bean
    @Primary
    public AdminUserController adminUserController() {
        return new AdminUserController(mock(UserService.class));
    }

}

