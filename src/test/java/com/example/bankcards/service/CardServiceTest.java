package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequestDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("тесты логики 'CardService'")
class CardServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardEncryptionUtil cardEncryptionUtil;

    @InjectMocks
    private CardService cardService; // РЕАЛЬНЫЙ сервис

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCardWithValidData() {
        Long userId = 1L;
        String plainNumber = "4111111111111111"; // валидный номер
        String encryptedNumber = "encrypted_1111";
        User user = new User("u", "p", "e@example.com", Role.USER);
        user.setId(userId);

        CardCreateRequestDTO dto = new CardCreateRequestDTO();
        dto.setCardNumber(plainNumber);
        dto.setExpireDate(LocalDate.of(2028, 12, 31));
        dto.setBalance(BigDecimal.ZERO);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardEncryptionUtil.encrypt(plainNumber)).thenReturn(encryptedNumber);
        when(cardRepository.existsByCardNumber(encryptedNumber)).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.createCard(dto, userId);

        assertNotNull(result);
        assertEquals(encryptedNumber, result.getCardNumber());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCardWithInvalidCardNumber() {
        Long userId = 1L;
        String invalidNumber = "1234567890123456"; // невалидный
        User user = new User();
        user.setId(userId);

        CardCreateRequestDTO dto = new CardCreateRequestDTO();
        dto.setCardNumber(invalidNumber);
        dto.setExpireDate(LocalDate.now().plusYears(1));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
                cardService.createCard(dto, userId));
    }

    @Test
    void deleteCardWithZeroBalance() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setBalance(BigDecimal.ZERO);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.deleteCard(cardId);

        verify(cardRepository).delete(card);
    }

    @Test
    void deleteCardWithNonZeroBalance() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setBalance(new BigDecimal("100.00"));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(IllegalStateException.class, () ->
                cardService.deleteCard(cardId));
    }

    @Test
    void deleteCardWithNonExistentCard() {
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                cardService.deleteCard(cardId));
    }

    @Test
    void blockCardWithActiveCard() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setCardStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.blockCard(cardId);

        assertEquals(CardStatus.BLOCKED, result.getCardStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCardWithAlreadyBlockedCard() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(IllegalStateException.class, () ->
                cardService.blockCard(cardId));
    }

    @Test
    void blockCardWithExpiredCard() {

        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setCardStatus(CardStatus.EXPIRED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        assertThrows(IllegalStateException.class, () ->
                cardService.blockCard(cardId));
    }

    @Test
    void activateCardWithBlockedCard() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.activateCard(cardId);

        assertEquals(CardStatus.ACTIVE, result.getCardStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void activateCardWithAlreadyActive() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setCardStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        assertThrows(IllegalStateException.class, () ->
                cardService.activateCard(cardId));
    }

    @Test
    void activateCardWithExpiredCard() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setCardStatus(CardStatus.EXPIRED);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        assertThrows(IllegalStateException.class, () ->
                cardService.activateCard(cardId));
    }

    @Test
    void getAllCards() {
        List<Card> expectedCards = Arrays.asList(new Card(), new Card());
        when(cardRepository.findAll()).thenReturn(expectedCards);
        List<Card> result = cardService.getAllCards();
        assertEquals(expectedCards, result);
        verify(cardRepository).findAll();
    }

    @Test
    void getUserCards() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> expectedPage = new PageImpl<>(Arrays.asList(new Card(), new Card()));
        when(cardRepository.findByUserId(userId, pageable)).thenReturn(expectedPage);
        Page<Card> result = cardService.getUserCards(userId, pageable);
        assertEquals(expectedPage, result);
        verify(cardRepository).findByUserId(userId, pageable);
    }

    @Test
    void getCardByIdWithExistingCard() {
        Long cardId = 1L;
        Card expectedCard = new Card();
        expectedCard.setId(cardId);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(expectedCard));
        Optional<Card> result = cardService.getCardById(cardId);
        assertTrue(result.isPresent());
        assertEquals(expectedCard, result.get());
    }

    @Test
    void getCardByIdWithNonExistentCard() {
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());
        Optional<Card> result = cardService.getCardById(cardId);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCardBalanceWithExistingCard() {
        Long cardId = 1L;
        BigDecimal expectedBalance = new BigDecimal("1500.00");
        Card card = new Card();
        card.setId(cardId);
        card.setBalance(expectedBalance);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        BigDecimal result = cardService.getCardBalance(cardId);
        assertEquals(expectedBalance, result);
    }

    @Test
    void getCardBalanceWithNonExistentCard() {
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                cardService.getCardBalance(cardId));
    }
}
