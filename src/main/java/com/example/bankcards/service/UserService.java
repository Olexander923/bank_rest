package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.UserNameAlreadyExistException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CardRepository cardRepository;
    private final Role role;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CardRepository cardRepository, Role role) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cardRepository = cardRepository;
        this.role = role;
    }

    /**
     * создание пользователя
     */
    public User createUser(String username,String email,String password,Role role) throws UserNameAlreadyExistException, EmailAlreadyExistsException {
        Objects.requireNonNull(username,"username cannot be null");
        Objects.requireNonNull(email,"email cannot be null");

        if(userRepository.existsByUsername(username))
            throw new UserNameAlreadyExistException("User already exist");

        if(userRepository.existsByEmail(email))
            throw new EmailAlreadyExistsException("email already exist");
        String hashPassword = passwordEncoder.encode(password);
        Role newRole = Role.USER;
        var newUser = new User(username,email,hashPassword, newRole);

        return userRepository.save(newUser);
    }


    public Optional<User> findUserById(Long userId){
          return userRepository.findById(userId);
    }

    public Optional<User> findByUsername(String username){
      return userRepository.findByUsername(username);
    }

    /**
     * поиск карты
     */
    public Optional<Card> getUserCards(Long userId){
       return cardRepository.findByUserId(userId);
    }

    /**
     * обновление пользователя,только для админа
     */
    public User updateUser(Long userId,String newEmail,String newUsername) throws UserNameAlreadyExistException {
         var user = findUserById(userId).
                 orElseThrow(()-> new IllegalArgumentException("user with id: " + userId + " not found"));

         if(newEmail != null) user.setEmail(newEmail);

         if (newUsername != null) {
             if (userRepository.existsByUsername(newUsername)) {
                 throw new UserNameAlreadyExistException("User already exist");
             }
             user.setUsername(newUsername);
         }
             return userRepository.save(user);

    }

    public void deleteUser(Long userId) {
        if(!userRepository.existsById(userId))
                throw new IllegalArgumentException("user with id: " + userId + " not found");
        boolean hasActiveCards = cardRepository.existsByUserIdAndCardStatus(userId, CardStatus.ACTIVE);
        if (hasActiveCards) throw new IllegalStateException("Cannot delete user with active card");
         userRepository.deleteById(userId);
    }
}
