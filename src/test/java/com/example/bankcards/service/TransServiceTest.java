package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardService cardService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(testUser);
        fromCard.setCardStatus(CardStatus.ACTIVE);
        fromCard.setExpireDate(LocalDate.now().plusYears(1));
        fromCard.setBalance(new BigDecimal("1000.00"));

        toCard = new Card();
        toCard.setId(2L);
        toCard.setUser(testUser);
        toCard.setCardStatus(CardStatus.ACTIVE);
        toCard.setExpireDate(LocalDate.now().plusYears(1));
        toCard.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void transferWithValidData()  {
        BigDecimal transferAmount = new BigDecimal("300.00");
        Long userId = testUser.getId();

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transferService.transferBetweenCards(fromCard.getId(), toCard.getId(), userId, transferAmount);

        assertEquals(new BigDecimal("700.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("800.00"), toCard.getBalance());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transferCardsWithNegativeAmount() {
        // Given
        BigDecimal negativeAmount = new BigDecimal("-100.00");
        Long userId = testUser.getId();
        assertThrows(IllegalArgumentException.class, () ->
                transferService.transferBetweenCards(fromCard.getId(), toCard.getId(), userId, negativeAmount));
    }

    @Test
    void transferWithZeroAmount() {
        BigDecimal zeroAmount = BigDecimal.ZERO;
        Long userId = testUser.getId();

        assertThrows(IllegalArgumentException.class, () ->
                transferService.transferBetweenCards(fromCard.getId(), toCard.getId(), userId, zeroAmount));
    }

    @Test
    void transferWithNonExistentFromCard() {

        Long nonExistentCardId = 999L;
        Long userId = testUser.getId();
        BigDecimal transferAmount = new BigDecimal("100.00");

        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThrows(IllegalArgumentException.class, () ->
                transferService.transferBetweenCards(nonExistentCardId, toCard.getId(), userId, transferAmount));
    }

    @Test
    void transferWithNonExistentToCard() {
        Long nonExistentCardId = 999L;
        Long userId = testUser.getId();
        BigDecimal transferAmount = new BigDecimal("100.00");

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                transferService.transferBetweenCards(fromCard.getId(), nonExistentCardId, userId, transferAmount));
    }

    @Test
    void transferWithInactiveFromCard() {

        Long userId = testUser.getId();
        BigDecimal transferAmount = new BigDecimal("100.00");
        fromCard.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                transferService.transferBetweenCards(fromCard.getId(), toCard.getId(), userId, transferAmount));
    }

    @Test
    void transferWithInactiveToCard() {
        Long userId = testUser.getId();
        BigDecimal transferAmount = new BigDecimal("100.00");
        toCard.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThrows(IllegalStateException.class, () ->
                transferService.transferBetweenCards(fromCard.getId(), toCard.getId(), userId, transferAmount));
    }

    @Test
    void transferWithExpiredFromCard() {
        Long userId = testUser.getId();
        BigDecimal transferAmount = new BigDecimal("100.00");
        fromCard.setExpireDate(LocalDate.now().minusDays(1));

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThrows(IllegalStateException.class, () ->
                transferService.transferBetweenCards(fromCard.getId(), toCard.getId(), userId, transferAmount));
    }

    @Test
    void transferWithExpiredToCard_() {
        Long userId = testUser.getId();
        BigDecimal transferAmount = new BigDecimal("100.00");
        toCard.setExpireDate(LocalDate.now().minusDays(1));

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThrows(IllegalStateException.class, () ->
                transferService.transferBetweenCards(fromCard.getId(), toCard.getId(), userId, transferAmount));
    }

    @Test
    void transferWithDifferentUserCard() {

        User differentUser = new User();
        differentUser.setId(2L);
        toCard.setUser(differentUser); // карта другого пользователя

        Long userId = testUser.getId();
        BigDecimal transferAmount = new BigDecimal("100.00");

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThrows(SecurityException.class, () ->
                transferService.transferBetweenCards(fromCard.getId(), toCard.getId(), userId, transferAmount));
    }

    @Test
    void transferWithInsufficientFunds() {

        Long userId = testUser.getId();
        BigDecimal excessiveAmount = new BigDecimal("1500.00"); // Больше чем баланс

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThrows(IllegalStateException.class, () ->
                transferService.transferBetweenCards(fromCard.getId(), toCard.getId(), userId, excessiveAmount));
    }

    @Test
    void transferWithExactBalance()  {

        Long userId = testUser.getId();
        BigDecimal exactAmount = new BigDecimal("1000.00"); // Равно балансу

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transferService.transferBetweenCards(fromCard.getId(), toCard.getId(), userId, exactAmount);
        assertEquals(0, fromCard.getBalance().compareTo(BigDecimal.ZERO));
        assertEquals(0, toCard.getBalance().compareTo(new BigDecimal("1500.00")));
        verify(transactionRepository).save(any(Transaction.class));
    }
}