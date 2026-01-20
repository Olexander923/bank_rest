package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequestDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.constants.CardStatus;
import com.example.bankcards.constants.Role;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("tests logic 'CardService'")
class CardServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardEncryptionUtil cardEncryptionUtil;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCardWithValidData() {
        String plainNumber = "4111111111111111"; // валидный номер
        String encryptedNumber = "encrypted_1111";
        User user = new User("u", "p", "e@example.com", Role.USER);
        user.setId(1L);

        CardCreateRequestDTO dto = new CardCreateRequestDTO();
        dto.setCardNumber(plainNumber);
        dto.setExpireDate(LocalDate.of(2028, 12, 31));
        dto.setBalance(BigDecimal.ZERO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardEncryptionUtil.encrypt(plainNumber)).thenReturn(encryptedNumber);
        when(cardRepository.existsByCardNumber(encryptedNumber)).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.createCard(dto, 1L);

        assertNotNull(result);
        assertEquals(encryptedNumber, result.getCardNumber());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCardWithInvalidCardNumber() {
        String invalidNumber = "1234567890123456"; // невалидный
        User user = new User();
        user.setId(1L);

        CardCreateRequestDTO dto = new CardCreateRequestDTO();
        dto.setCardNumber(invalidNumber);
        dto.setExpireDate(LocalDate.now().plusYears(1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
                cardService.createCard(dto, 1L));
    }

    @Test
    void deleteCardWithZeroBalance() {
        Card card = new Card();
        card.setId(1L);
        card.setBalance(BigDecimal.ZERO);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard(1L);

        verify(cardRepository).delete(card);
    }

    @Test
    void deleteCardWithNonZeroBalance() {
        Card card = new Card();
        card.setId(1L);
        card.setBalance(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(IllegalStateException.class, () ->
                cardService.deleteCard(1L));
    }

    @Test
    void deleteCardWithNonExistentCard() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                cardService.deleteCard(999L));
    }

    @Test
    void blockCardWithActiveCard() {
        Card card = new Card();
        card.setId(1L);
        card.setCardStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.blockCard(1L);

        assertEquals(CardStatus.BLOCKED, result.getCardStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCardWithAlreadyBlockedCard() {
        Card card = new Card();
        card.setId(1L);
        card.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(IllegalStateException.class, () ->
                cardService.blockCard(1L));
    }

    @Test
    void blockCardWithExpiredCard() {
        Card card = new Card();
        card.setId(1L);
        card.setCardStatus(CardStatus.EXPIRED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        assertThrows(IllegalStateException.class, () ->
                cardService.blockCard(1L));
    }

    @Test
    void activateCardWithBlockedCard() {
        Card card = new Card();
        card.setId(1L);
        card.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.activateCard(1L);

        assertEquals(CardStatus.ACTIVE, result.getCardStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void activateCardWithAlreadyActive() {
        Card card = new Card();
        card.setId(1L);
        card.setCardStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        assertThrows(IllegalStateException.class, () ->
                cardService.activateCard(1L));
    }

    @Test
    void activateCardWithExpiredCard() {
        Card card = new Card();
        card.setId(1L);
        card.setCardStatus(CardStatus.EXPIRED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        assertThrows(IllegalStateException.class, () ->
                cardService.activateCard(1L));
    }

    @Test
    void getAllCards() {
        Pageable pageable = PageRequest.of(0,10);
        Page<Card> expectedPage = new PageImpl<>(Arrays.asList(new Card(),new Card()));
        when(cardRepository.findAll(pageable)).thenReturn(expectedPage);
        Page<Card> result = cardService.getAllCards(pageable);
        assertEquals(expectedPage, result);
        verify(cardRepository).findAll(pageable);
    }

    @Test
    void getUserCards() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> expectedPage = new PageImpl<>(Arrays.asList(new Card(), new Card()));
        when(cardRepository.findByUserId(1L, pageable)).thenReturn(expectedPage);
        Page<Card> result = cardService.getUserCards(1L, pageable);
        assertEquals(expectedPage, result);
        verify(cardRepository).findByUserId(1L, pageable);
    }

    @Test
    void getCardByIdWithExistingCard() {
        Card expectedCard = new Card();
        expectedCard.setId(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(expectedCard));
        Optional<Card> result = cardService.getCardById(1L);
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
    void getCardBalanceWithExistingCard()  {
        BigDecimal expectedBalance = new BigDecimal("1500.00");
        Card card = new Card();
        card.setId(1L);
        card.setBalance(expectedBalance);
        card.setExpireDate(LocalDate.now().plusYears(1));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        BigDecimal result = cardService.getCardBalance(1L);
        assertEquals(expectedBalance, result);
    }

    @Test
    void getCardBalanceWithNonExistentCard() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                cardService.getCardBalance(999L));
    }
}
