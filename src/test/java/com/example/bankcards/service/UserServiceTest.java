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

@DisplayName("test logic UserService")
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        Optional<User> result = userService.findUserById(1L);
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void findUserByIdWithNonExistentUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<User> result = userService.findUserById(999L);
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
        String newEmail = "newemail@example.com";
        String nullUsername = null;

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(1L, newEmail, nullUsername);

        assertEquals(newEmail, result.getEmail());
        assertEquals(testUser.getUsername(), result.getUsername()); // username не изменился
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserWithOnlyUsername() throws UserNameAlreadyExistException {
        String nullEmail = null;
        String newUsername = "newusername";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(1L, nullEmail, newUsername);

        assertEquals(testUser.getEmail(), result.getEmail()); // email не изменился
        assertEquals(newUsername, result.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserWithExistingUsername() {
        String newEmail = "newemail@example.com";
        String existingUsername = "existinguser";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(existingUsername)).thenReturn(true);

        assertThrows(UserNameAlreadyExistException.class, () ->
                userService.updateUser(1L, newEmail, existingUsername));
    }

    @Test
    void updateUserWithNonExistentUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                userService.updateUser(999L, "new@email.com", "newusername"));
    }

    @Test
    void deleteUserWithNoActiveCards() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.existsByUserIdAndCardStatus(1L, CardStatus.ACTIVE)).thenReturn(false);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserWithActiveCards() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.existsByUserIdAndCardStatus(1L, CardStatus.ACTIVE)).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                userService.deleteUser(1L));
    }

    @Test
    void deleteUserWithNonExistentUser() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                userService.deleteUser(999L));
    }
}

