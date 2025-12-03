package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.PasswordPolicyViolationException;
import com.example.bankcards.exception.UserNameAlreadyExistException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CardRepository cardRepository;

    /**
     * создание пользователя
     */
    public User createUser(String username, String email, String password, Role role)  {
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");

        if (userRepository.existsByUsername(username))
            throw new UserNameAlreadyExistException("User already exist");

        if (userRepository.existsByEmail(email))
            throw new EmailAlreadyExistsException("Email already exist");

//        if (!Validator.isValidPassword(password))
//            throw new PasswordPolicyViolationException("Password too weak!");

        String hashPassword = passwordEncoder.encode(password);
        Role newRole = role;
        var newUser = new User(username, hashPassword, email, newRole);

        return userRepository.save(newUser);
    }


    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }


    /**
     * обновление пользователя,только для админа
     */
    public User updateUser(Long userId, String email, String username) {
        var user = findUserById(userId).
                orElseThrow(() -> new IllegalArgumentException("User with id: " + userId + " not found"));

        if (email != null) user.setEmail(email);

        if (username != null) {
            if (userRepository.existsByUsername(username)) {
                throw new UserNameAlreadyExistException("User already exist");
            }
            user.setUsername(username);
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId))
            throw new IllegalArgumentException("User with id: " + userId + " not found");
        boolean hasActiveCards = cardRepository.existsByUserIdAndCardStatus(userId, CardStatus.ACTIVE);
        if (hasActiveCards) throw new IllegalStateException("Cannot delete user with active card");
        userRepository.deleteById(userId);
    }
}
