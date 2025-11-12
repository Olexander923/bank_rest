package com.example.bankcards.controller;

import com.example.bankcards.security.JwtUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
@EnableWebMvc
@ComponentScan(basePackages = "com.example.bankcards.controller")
public class TestWebConfig implements WebMvcConfigurer {
    @Bean
    public JwtUtils jwtUtils() {
        JwtUtils jwtUtils = new JwtUtils();
        // Устанавливаем свойства через сеттеры или рефлексию
        // Так как @Value не сработает в тестовой конфигурации
        try {
            java.lang.reflect.Field secretField = JwtUtils.class.getDeclaredField("secret");
            secretField.setAccessible(true);
            secretField.set(jwtUtils, "test-secret-key-12345678901234567890123456789012");

            java.lang.reflect.Field expirationField = JwtUtils.class.getDeclaredField("expiration");
            expirationField.setAccessible(true);
            expirationField.set(jwtUtils, 86400000L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set JWT properties", e);
        }
        return jwtUtils;
    }
}
