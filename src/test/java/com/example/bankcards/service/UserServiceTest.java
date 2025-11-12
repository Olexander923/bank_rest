package com.example.bankcards.service;

import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.UserNameAlreadyExistException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("тестирование логики UserService")
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() throws UserNameAlreadyExistException, EmailAlreadyExistsException {
        // Given
        String username = "newuser";
        String email = "new@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        User result = userService.createUser(username, email, password, Role.USER);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals(Role.USER, result.getRole());
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowException() {
        // Given
        String existingUsername = "existinguser";
        String email = "new@example.com";
        String password = "password123";

        when(userRepository.existsByUsername(existingUsername)).thenReturn(true);

        // When & Then
        assertThrows(UserNameAlreadyExistException.class, () ->
                userService.createUser(existingUsername, email, password, Role.USER));
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowException() {
        // Given
        String username = "newuser";
        String existingEmail = "existing@example.com";
        String password = "password123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () ->
                userService.createUser(username, existingEmail, password, Role.USER));
    }

    @Test
    void createUser_WithNullUsername_ShouldThrowException() {
        // Given
        String nullUsername = null;
        String email = "test@example.com";
        String password = "password123";

        // When & Then
        assertThrows(NullPointerException.class, () ->
                userService.createUser(nullUsername, email, password, Role.USER));
    }

    @Test
    void createUser_WithNullEmail_ShouldThrowException() {
        // Given
        String username = "testuser";
        String nullEmail = null;
        String password = "password123";

        // When & Then
        assertThrows(NullPointerException.class, () ->
                userService.createUser(username, nullEmail, password, Role.USER));
    }

    @Test
    void findUserById_WithExistingUser_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void findUserById_WithNonExistentUser_ShouldReturnEmpty() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findUserById(userId);

        // Then
        assertTrue(result.isEmpty());
    }


    @Test
    void updateUser_WithValidData_ShouldUpdateUser() throws UserNameAlreadyExistException {
        // Given
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String newUsername = "newusername";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = userService.updateUser(userId, newEmail, newUsername);

        // Then
        assertEquals(newEmail, result.getEmail());
        assertEquals(newUsername, result.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WithOnlyEmail_ShouldUpdateOnlyEmail() throws UserNameAlreadyExistException {
        // Given
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String nullUsername = null;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = userService.updateUser(userId, newEmail, nullUsername);

        // Then
        assertEquals(newEmail, result.getEmail());
        assertEquals(testUser.getUsername(), result.getUsername()); // username не изменился
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WithOnlyUsername_ShouldUpdateOnlyUsername() throws UserNameAlreadyExistException {
        // Given
        Long userId = 1L;
        String nullEmail = null;
        String newUsername = "newusername";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = userService.updateUser(userId, nullEmail, newUsername);

        // Then
        assertEquals(testUser.getEmail(), result.getEmail()); // email не изменился
        assertEquals(newUsername, result.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WithExistingUsername_ShouldThrowException() {
        // Given
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String existingUsername = "existinguser";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(existingUsername)).thenReturn(true);

        // When & Then
        assertThrows(UserNameAlreadyExistException.class, () ->
                userService.updateUser(userId, newEmail, existingUsername));
    }

    @Test
    void updateUser_WithNonExistentUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                userService.updateUser(userId, "new@email.com", "newusername"));
    }

    @Test
    void deleteUser_WithNoActiveCards_ShouldDeleteUser() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(cardRepository.existsByUserIdAndCardStatus(userId, CardStatus.ACTIVE)).thenReturn(false);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WithActiveCards_ShouldThrowException() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(cardRepository.existsByUserIdAndCardStatus(userId, CardStatus.ACTIVE)).thenReturn(true);

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                userService.deleteUser(userId));
    }

    @Test
    void deleteUser_WithNonExistentUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                userService.deleteUser(userId));
    }
}

