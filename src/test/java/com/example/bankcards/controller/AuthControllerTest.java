package com.example.bankcards.controller;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(AuthController.class)
@Import({AdminUserControllerTest.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JwtUtils jwtUtils;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_return200() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.tokenGeneration(any())).thenReturn("test_token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"ValidPass1@\"}")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void loginExpectedTokenGeneration_return200() throws Exception {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.tokenGeneration(any(UserDetails.class))).thenReturn("expected_token");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"ValidPass1@\"}")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("expected_token"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
//тут ошибка аутентификации
    void loginInvalidPassword_return401() throws Exception {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
        when(jwtUtils.tokenGeneration(any(UserDetails.class))).thenReturn("test_token");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"Wrong_password1@\"}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Invalid credentials"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
//тут ошибка клиента
    void loginIncorrectPassword_return400() throws Exception {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.tokenGeneration(any(UserDetails.class))).thenReturn("test_token");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"wrong_\"}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("The password must be exactly min 8 characters long!"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
//ошибка аутентификации
    void loginUserNotExist_return401() throws Exception {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials, user not found!"));
        when(jwtUtils.tokenGeneration(any(UserDetails.class))).thenReturn("test_token");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"test_token\"}")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Bad credentials, user not found!"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
//ошибка клиента
    void loginUserNotExist_return400() throws Exception {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.tokenGeneration(any(UserDetails.class))).thenReturn("test_token");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"test_token\"}")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("The user name must be exactly min 8 characters long!"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
//ошибка клиента
    void loginEmptyCredentials_return400() throws Exception {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.tokenGeneration(any(UserDetails.class))).thenReturn("test_token");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        containsString("must be exactly min 8 characters")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void loginBlockedAccount_return403() throws Exception {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new LockedException("This account is blocked!"));
        when(jwtUtils.tokenGeneration(any(UserDetails.class))).thenReturn("test_token");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"test_token\"}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("This account is blocked!"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void loginAccountExpired_return403() throws Exception {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new AccountExpiredException("Account expired!"));
        when(jwtUtils.tokenGeneration(any(UserDetails.class))).thenReturn("test_token");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"test_token\"}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Account expired!"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void login_return500() throws Exception {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.tokenGeneration(any(UserDetails.class)))
                .thenThrow(new RuntimeException("Token generation failed!"));
        when(authentication.getPrincipal()).thenReturn(userDetails);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"test_token\"}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Token generation failed!"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @DisplayName("method 'register' tests")
    @Test
    void register_return200() throws Exception {
        User user = new User("testuser", "password", "test@example.com", Role.ADMIN);
        user.setId(1L);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encode password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """ 
                                        {
                                            "username": "testuser",
                                            "email": "test@example.com",
                                            "password": "ValidPass1@"
                                        }
                                        """)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.jsonPath("$")
                        .exists())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void registerUserAlreadyBusy_return400() throws Exception {
        User user = new User("testuser", "password", "test@example.com", Role.ADMIN);
        user.setId(1L);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("encode password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """ 
                                        {
                                            "username": "testuser",
                                            "email": "test@example.com",
                                            "password": "password"
                                        }
                                        """)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.jsonPath("$")
                        .exists())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void registerEmailAlreadyExist_return409() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.ADMIN);
        user.setId(1L);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """ 
                                        {
                                            "username": "testuser",
                                            "email": "test@example.com",
                                            "password": "ValidPass1@"
                                        }
                                        """)
                        .with(csrf()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("This email already exist!"));
    }

    @Test
//тут ошибка клиента
    void registerIncorrectEmailFormat_return400() throws Exception {
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """ 
                                        {
                                            "username": "testuser",
                                            "email": "test",
                                            "password": "ValidPass1@"
                                        }
                                        """)
                        .with(csrf()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Email should be valid"));
    }

    @Test
    void registerInvalidUsernameFormat_return400() throws Exception {
        when(userRepository.existsByUsername("test")).thenReturn(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """ 
                                        {
                                            "username": "test",
                                            "email": "test@example.com",
                                            "password": "ValidPass1@"
                                        }
                                        """)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("The user name must be exactly min 8 characters long!"));
    }

    @Test
    void registerTooWeakPassword_return400() throws Exception {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenThrow(new PasswordPolicyViolationException("Password too weak!"));
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """ 
                                        {
                                            "username": "testuser",
                                            "email": "test@example.com",
                                            "password": "123456789"
                                        }
                                        """)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Password too weak! Password must contain lowercase,uppercase, digit, special char @#$%, 8-16 chars"));
    }

    @Test
    void registerEmptyFields_return400() throws Exception {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encode password");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """ 
                                        {
                                            "username": "short",
                                            "email": "test@example.com",
                                            "password": "ValidPass1@"
                                        }
                                        """)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        containsString("must be exactly min 8 characters")));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """ 
                                        {
                                            "username": "testuser",
                                            "email": "",
                                            "password": "ValidPass1@"
                                        }
                                        """)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        containsString("Email cannot be blank")));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """ 
                                        {
                                            "username": "testuser",
                                            "email": "test@example.com",
                                            "password": ""
                                        }
                                        """)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        containsString("Password too weak")));
    }

    @Test
    void registerForbiddenSymbolsInUserName_return400() throws Exception {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """ 
                                        {
                                            "username": "tes tus-er",
                                            "email": "test@example.com",
                                            "password": "ValidPass1@"
                                        }
                                        """)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Username can only contain letters, numbers and underscores"));
    }

    @Test
    void registerDoesNotSaveInDB_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("ValidPass1@")).thenReturn("encode password");
        when(userRepository.save(any(User.class))).thenThrow(new DataAccessException("Database error!") {
            @Override
            public Throwable getRootCause() {
                return super.getRootCause();
            }
        });

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """ 
                                        {
                                            "username": "testuser",
                                            "email": "test@example.com",
                                            "password": "ValidPass1@"
                                        }
                                        """)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Database error!"));
    }

}
