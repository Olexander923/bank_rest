package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequestDTO;
import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Создавать, блокировать, активировать, удалять карты
 *
 * Видеть все карты (для ADMIN)
 *
 * Просматривать карты пользователя (поиск + пагинация)
 *
 * Запрашивать блокировку карты
 * R (Read) в CRUD для карт:
 *
 * getAllCards() - все карты (для ADMIN)
 *
 * getUserCards() - карты пользователя (для USER)
 *
 * getCardById() - конкретная карта
 *
 * getCardBalance() - баланс карты
 */
@Service
public class CardService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CardRepository cardRepository;


    public CardService(UserRepository userRepository, PasswordEncoder passwordEncoder, CardRepository cardRepository, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cardRepository = cardRepository;
    }
    /**
     * создание карты
     */
    public Card createCard(CardCreateRequestDTO cardCreateRequest,Long userId){
        User user = userRepository.findById(userId).orElseThrow(()->
                new IllegalArgumentException("User with id: " + userId + " already exist"));

        if (!cardRepository.existsByCardNumber(cardCreateRequest.getCardNumber()))
            throw new IllegalArgumentException("Card with cardNumber: " + cardCreateRequest + " already exist");

        if(Validator.isCardValidLuhn(cardCreateRequest.getCardNumber())) {
            throw new IllegalArgumentException("Invalid card number");
        }
        String encryptedCardNumber = passwordEncoder.encode(cardCreateRequest.getCardNumber());

       Card newCard = new Card(
               encryptedCardNumber,
               LocalDate.now().plusYears(4),
               CardStatus.ACTIVE,
               BigDecimal.ZERO,
               user
       );

        return cardRepository.save(newCard);
    }

    /**
     * ,удаление карты
     */
    public void deleteCard(Long cardId){
        Card card = cardRepository.findById(cardId)
                .orElseThrow(()-> new IllegalArgumentException("Card with id: " + cardId + " not found"));

        if (card.getBalance().compareTo(BigDecimal.ZERO) !=0)
            throw new IllegalStateException("Cannot delete card with non-zero balance");

        cardRepository.delete(card);
    }

    /**
     * блокировка карты
     */
    public Card blockCard(Long cardId){
        Card card = cardRepository.findById(cardId)
                .orElseThrow(()-> new IllegalArgumentException("Card with id: " + cardId + " not found"));
        if(card.getCardStatus() != CardStatus.ACTIVE)
            throw new IllegalStateException("Only active cards can be blocked");

        card.setCardStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    /**
     * активация
     */
    public Card activeCard(Long cardId){
        Card card = cardRepository.findById(cardId)
                .orElseThrow(()-> new IllegalArgumentException("Card with id: " + cardId + " not found"));

        if (card.getCardStatus() == CardStatus.ACTIVE)
            throw new IllegalStateException("Card is already active.");
        if (card.getCardStatus() != CardStatus.BLOCKED)
            throw new IllegalStateException("Only block cards can be activated.");
        card.setCardStatus(CardStatus.ACTIVE);

        return cardRepository.save(card);
    }

    /**
     * видеть все карты(только admin)
     */
    public List<Card> getAllCards(){
        List<Card> allCards = cardRepository.findAll();
        return allCards;
    }

    /**
     * просматривать карты пользователя (поиск + пагинация)
     */
    public Page<Card> getUserCards(Long userId, Pageable pageable){
        return cardRepository.findByUserId(userId,pageable);
    }

    /**
     *  конкретная карта
     */
    public Optional<Card> getCardById(Long cardId){
        return cardRepository.findById(cardId);
    }

    /**
     *  баланс карты
     */
    public BigDecimal getCardBalance(Long cardId) {
        var card = cardRepository.findById(cardId).orElseThrow(()->
                new IllegalArgumentException("No such card with id=%s "
                        .formatted(cardId)));
        return card.getBalance();
    }

}
