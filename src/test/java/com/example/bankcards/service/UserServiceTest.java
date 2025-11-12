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
    void createUserWithValidData() throws UserNameAlreadyExistException, EmailAlreadyExistsException {
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
        User result = userService.createUser(username, email, password, Role.USER);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals(Role.USER, result.getRole());
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserWithExistingUsername() {
        String existingUsername = "existinguser";
        String email = "new@example.com";
        String password = "password123";

        when(userRepository.existsByUsername(existingUsername)).thenReturn(true);

        assertThrows(UserNameAlreadyExistException.class, () ->
                userService.createUser(existingUsername, email, password, Role.USER));
    }

    @Test
    void createUserWithExistingEmail() {
        String username = "newuser";
        String existingEmail = "existing@example.com";
        String password = "password123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () ->
                userService.createUser(username, existingEmail, password, Role.USER));
    }

    @Test
    void createUserWithNullUsername() {
        String nullUsername = null;
        String email = "test@example.com";
        String password = "password123";

        assertThrows(NullPointerException.class, () ->
                userService.createUser(nullUsername, email, password, Role.USER));
    }

    @Test
    void createUserWithNullEmail() {
        String username = "testuser";
        String nullEmail = null;
        String password = "password123";

        assertThrows(NullPointerException.class, () ->
                userService.createUser(username, nullEmail, password, Role.USER));
    }

    @Test
    void findUserByIdWithExistingUser() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void findUserByIdWithNonExistentUser() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserById(userId);

        assertTrue(result.isEmpty());
    }


    @Test
    void updateUserWithValidData() throws UserNameAlreadyExistException {
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String newUsername = "newusername";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(userId, newEmail, newUsername);

        assertEquals(newEmail, result.getEmail());
        assertEquals(newUsername, result.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserWithOnlyEmail() throws UserNameAlreadyExistException {
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String nullUsername = null;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(userId, newEmail, nullUsername);

        assertEquals(newEmail, result.getEmail());
        assertEquals(testUser.getUsername(), result.getUsername()); // username не изменился
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserWithOnlyUsername() throws UserNameAlreadyExistException {
        Long userId = 1L;
        String nullEmail = null;
        String newUsername = "newusername";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(userId, nullEmail, newUsername);

        assertEquals(testUser.getEmail(), result.getEmail()); // email не изменился
        assertEquals(newUsername, result.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserWithExistingUsername() {
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String existingUsername = "existinguser";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(existingUsername)).thenReturn(true);

        assertThrows(UserNameAlreadyExistException.class, () ->
                userService.updateUser(userId, newEmail, existingUsername));
    }

    @Test
    void updateUserWithNonExistentUser() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                userService.updateUser(userId, "new@email.com", "newusername"));
    }

    @Test
    void deleteUserWithNoActiveCards() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(cardRepository.existsByUserIdAndCardStatus(userId, CardStatus.ACTIVE)).thenReturn(false);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUserWithActiveCards() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(cardRepository.existsByUserIdAndCardStatus(userId, CardStatus.ACTIVE)).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                userService.deleteUser(userId));
    }

    @Test
    void deleteUserWithNonExistentUser() {
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                userService.deleteUser(userId));
    }
}

