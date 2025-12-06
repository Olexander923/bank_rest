package com.example.bankcards.controller;

import com.example.bankcards.controller.test_security_configs.AdminUserControllerTestConfig;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.exception.UserNameAlreadyExistException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.CustomUserDetailService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtUtils;
import com.example.bankcards.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import({AdminUserControllerTestConfig.class, GlobalExceptionHandler.class})
@WithMockUser(roles = "ADMIN")
public class AdminUserControllerTest {
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private CardRepository cardRepository;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private CustomUserDetailService customUserDetailService;
    @MockitoBean
    private JwtUtils jwtUtils;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws ServletException, IOException {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void createUsers_return200() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.ADMIN);
        user.setId(1L);

        when(userService.createUser(eq("testuser"), eq("ValidPass1@"), eq("test@example.com"), eq(Role.ADMIN)))
                .thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "testuser",
                                    "password": "ValidPass1@",
                                    "email": "test@example.com",
                                    "role": "ADMIN"           
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
        verify(userService, times(1))
                .createUser(eq("testuser"), eq("ValidPass1@"), eq("test@example.com"), eq(Role.ADMIN));

    }

    @Test
    void createUser_return400_invalidPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON) // передаю слишком короткий пароль
                        .content("""
                                {
                                    "username": "testuser",
                                    "password": "pass", 
                                    "email": "test@example.com",
                                    "role": "ADMIN"           
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Password too weak! Password must contain lowercase,uppercase, digit, special char @#$%, 8-16 chars"));

    }

    @Test
    void createUserWithDuplication_return409() throws Exception {
        User user = new User("testuser", "password", "test@example.com", Role.ADMIN);
        user.setId(1L);

        when(userService.createUser(eq("testuser"), eq("ValidPass1@"), eq("test@example.com"), eq(Role.ADMIN)))
                .thenThrow(new UserNameAlreadyExistException("User with this username already exist!"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "testuser",
                                    "password": "ValidPass1@",
                                    "email": "test@example.com",
                                    "role": "ADMIN"           
                                }
                                """))
                .andExpect(status().isConflict())
                .andDo(print())
                .andExpect(jsonPath("$.message")
                        .value("User with this username already exist!"));
    }

    @DisplayName("method 'updateUsers' tests")
    @Test
    void updateUsers_return200() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.ADMIN);
        user.setId(1L);

        when(userService.updateUser(eq(1L), eq("testuser"), eq("test@example.com")))
                .thenReturn(user);

        mockMvc.perform(put("/api/admin/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "testuser",
                                    "userId": 1,
                                    "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
        verify(userService, times(1))
                .updateUser(eq(1L), eq("testuser"), eq("test@example.com"));

    }

    @Test
    void updateUsers_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.ADMIN);
        user.setId(1L);

        mockMvc.perform(put("/api/admin/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "",
                                    "userId": 1,
                                    "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User cannot be blank"));
    }

    @Test
    void updateUsersWithDuplicationUsername_return409() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.ADMIN);
        user.setId(1L);

        when(userService.updateUser(eq(1L), eq("testuser"), eq("test@example.com")))
                .thenThrow(new UserNameAlreadyExistException("User with this username already exist!"));

        mockMvc.perform(put("/api/admin/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "testuser",
                                    "userId": 1,
                                    "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("User with this username already exist!"));
    }

    @Test
    void updateUsersWithDuplicationEmail_return409() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.ADMIN);
        user.setId(1L);

        when(userService.updateUser(eq(1L), eq("testuser"), eq("test@example.com")))
                .thenThrow(new EmailAlreadyExistsException("User with this email already exist!"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "testuser",
                                    "userId": 1,
                                    "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("User with this email already exist!"));
    }

    @Test
    void updateUsersForNotExistingUser_return404() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.ADMIN);
        user.setId(999L);

        when(userService.updateUser(eq(999L), eq("testuser"), eq("test@example.com")))
                .thenThrow(new UserNotFoundException("User with this id not found!"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "testuser",
                                    "userId": 999,
                                    "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with this id not found!"));
    }

    @Test
    void updateUsersWithInvalidEmail_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.ADMIN);
        user.setId(1L);

        when(userService.updateUser(eq(1L), eq("testuser"), eq("test@example.com")))
                .thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "testuser",
                                    "userId": 1,
                                    "email": "invalid-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email should be valid"));
    }

    @Test
    void updateUsers_invalidUsername_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.ADMIN);
        user.setId(1L);

        when(userService.updateUser(eq(1L), eq("testuser"), eq("test@example.com")))
                .thenReturn(user);
        ;

        mockMvc.perform(put("/api/admin/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "te",
                                    "userId": 1,
                                    "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Username must be between 3 and 50 characters"));
    }

    @Test
    void updateUsersWithoutJsonField_return400() throws Exception {
        mockMvc.perform(put("/api/admin/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                
                                    "userId": 1,
                                    "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User cannot be blank"));
    }

    @DisplayName("method 'deleteUsers' tests")
    @Test
    void deleteUsers_return204() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test//проверка что api отвечает не неправильный формат id
    void deleteUsers_API_return400() throws Exception {
        mockMvc.perform(delete("/api/admin/users/gher")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test//проверка что id нет в бд
    void deleteUsers_return404() throws Exception {
        doThrow(new UserNotFoundException("User with this id not found!")).when(userService)
                .deleteUser(9999L);
        mockMvc.perform(delete("/api/admin/users/9999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
