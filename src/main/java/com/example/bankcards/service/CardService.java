package com.example.bankcards.service;

import com.example.bankcards.constants.RequestStatus;
import com.example.bankcards.dto.BlockRequestDTO;
import com.example.bankcards.dto.CardCreateRequestDTO;
import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.constants.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardExpiredException;
import com.example.bankcards.repository.BlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.BlockRequestMapper;
import com.example.bankcards.util.CardEncryptionUtil;
import com.example.bankcards.util.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CardService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CardEncryptionUtil cardEncryptionUtil;
    private final BlockRequestRepository blockRequestRepository;
    private final BlockRequestMapper requestMapper;

    /**
     * создание карты
     */
    public Card createCard(CardCreateRequestDTO cardCreateRequest, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("User with id: " + userId + " not found"));

        String cardNumber = cardCreateRequest.getCardNumber();
        String encryptedCardNumber = cardEncryptionUtil.encrypt(cardNumber);

        if (cardRepository.existsByCardNumber(encryptedCardNumber)) {
            throw new IllegalArgumentException("Card with cardNumber ending in " + cardNumber.substring(cardNumber.length() - 4) + " already exists");
        }

        if (!Validator.isCardValidLuhn(cardCreateRequest.getCardNumber())) {
            throw new IllegalArgumentException("Invalid card number");
        }

        Card newCard = new Card(
                encryptedCardNumber,
                cardCreateRequest.getExpireDate(),
                CardStatus.ACTIVE,
                cardCreateRequest.getBalance(),
                user
        );

        return cardRepository.save(newCard);
    }

    /**
     * удаление карты
     */
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card with id: " + cardId + " not found"));

        if (card.getBalance().compareTo(BigDecimal.ZERO) != 0)
            throw new IllegalStateException("Cannot delete card with non-zero balance");

        cardRepository.delete(card);
    }

    /**
     * блокировка карты
     */
    public Card blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card with id: " + cardId + " not found"));
        if (card.getCardStatus() != CardStatus.ACTIVE)
            throw new IllegalStateException("Only active cards can be blocked");

        card.setCardStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    /**
     * запрос на блокировку от пользователя
     */
     public BlockRequestDTO requestBlockCard(Long cardId,Long userId) {
         Card card = cardRepository.findById(cardId)
                 .orElseThrow(() -> new IllegalArgumentException("Card with id: " + cardId + " not found"));
         if (!card.getUser().getId().equals(userId)) {
             throw new IllegalStateException("Сard doesn't belong to user");
         }
         BlockRequest blockRequest = new BlockRequest();
         blockRequest.setCard(card);
         blockRequest.setUser(card.getUser());
         blockRequest.setRequestDate(LocalDateTime.now());
         blockRequest.setRequestStatus(RequestStatus.PENDING);
         BlockRequest savedRequest = blockRequestRepository.save(blockRequest);
         return requestMapper.requestToDTO(savedRequest);
     }

    /**
     * активация
     */
    public Card activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card with id: " + cardId + " not found"));

        if (card.getCardStatus() == CardStatus.ACTIVE)
            throw new IllegalStateException("Card is already active.");
        if (card.getCardStatus() != CardStatus.BLOCKED)
            throw new IllegalStateException("Only block cards can be activated.");
        card.setCardStatus(CardStatus.ACTIVE);

        return cardRepository.save(card);
    }

    /**
     * видеть все карты(только admin) + паганация
     */
    public Page<Card> getAllCardsByStatus(CardStatus status, Pageable pageable) {
        return cardRepository.findByCardStatus(status,pageable);
    }

    /**
     * просматривать карты пользователя (поиск + пагинация)
     */
    public Page<Card> getUserCards(Long userId, Pageable pageable) {
        return cardRepository.findByUserId(userId, pageable);
    }

    /**
     * просмотреть все карты вообще + паганация
     */
    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    /**
     * конкретная карта
     */
    public Optional<Card> getCardById(Long cardId) {
        return cardRepository.findById(cardId);
    }

    /**
     * получение всех карт с истекающим сроком, с паганацией(только admin)
     */
    public Page<Card> getCardsWithExpiringDateBefore(LocalDate date,Pageable pageable){
        return cardRepository.findByExpireDateBefore(date, pageable);
    }

    /**
     * баланс карты
     */
    public BigDecimal getCardBalance(Long cardId) {
        var card = cardRepository.findById(cardId).orElseThrow(() ->
                new IllegalArgumentException("No such card with id=%s "
                        .formatted(cardId)));

        if (card.getCardStatus() == CardStatus.BLOCKED)
            throw new CardBlockedException("Cannot get balance for blocked card");

        if (card.getExpireDate().isBefore(LocalDate.now()))
            throw new CardExpiredException("Card expired!");

        return card.getBalance();
    }

}
