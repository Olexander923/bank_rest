package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.TransferFailedException;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.util.CardMapper;
import com.example.bankcards.util.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCardControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private TransferService transferService;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private UserCardController userCardController;

    @Test
    void getUserCardsWithValidUser() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Card card1 = new Card();
        card1.setId(1L);
        Card card2 = new Card();
        card2.setId(2L);
        List<Card> cards = Arrays.asList(card1, card2);
        Page<Card> cardPage = new PageImpl<>(cards, pageable, cards.size());

        CardResponseDTO dto1 = new CardResponseDTO();
        dto1.setId(1L);
        CardResponseDTO dto2 = new CardResponseDTO();
        dto2.setId(2L);

        when(userDetails.getUserId()).thenReturn(userId);
        when(cardService.getUserCards(userId, pageable)).thenReturn(cardPage);
        when(cardMapper.toDTO(card1)).thenReturn(dto1);
        when(cardMapper.toDTO(card2)).thenReturn(dto2);

        ResponseEntity<Page<CardResponseDTO>> result = userCardController.getUserCards(userDetails, pageable);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().getContent().size());
        assertEquals(1L, result.getBody().getContent().get(0).getId());
        assertEquals(2L, result.getBody().getContent().get(1).getId());
        verify(cardService).getUserCards(userId, pageable);
    }

    @Test
    void getCardBalanceWithOwnCard() {
        Long userId = 1L;
        Long cardId = 1L;
        BigDecimal expectedBalance = new BigDecimal("1500.50");

        User user = new User();
        user.setId(userId);

        Card card = new Card();
        card.setId(cardId);
        card.setUser(user);

        when(userDetails.getUserId()).thenReturn(userId);
        when(cardService.getCardById(cardId)).thenReturn(Optional.of(card));
        when(cardService.getCardBalance(cardId)).thenReturn(expectedBalance);

        ResponseEntity<BigDecimal> result = userCardController.getCardBalance(cardId, userDetails);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedBalance, result.getBody());
        verify(cardService).getCardById(cardId);
        verify(cardService).getCardBalance(cardId);
    }

    @Test
    void getCardBalanceWithNonExistentCard() {
        Long cardId = 999L;

        when(cardService.getCardById(cardId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                userCardController.getCardBalance(cardId, userDetails));

        verify(cardService).getCardById(cardId);
        verify(cardService, never()).getCardBalance(any());
    }

    @Test
    void getCardBalanceWithOtherUsersCard() {

        Long currentUserId = 1L;
        Long otherUserId = 2L;
        Long cardId = 1L;

        User otherUser = new User();
        otherUser.setId(otherUserId);

        Card card = new Card();
        card.setId(cardId);
        card.setUser(otherUser);

        when(userDetails.getUserId()).thenReturn(currentUserId);
        when(cardService.getCardById(cardId)).thenReturn(Optional.of(card));

        assertThrows(SecurityException.class, () ->
                userCardController.getCardBalance(cardId, userDetails));

        verify(cardService).getCardById(cardId);
        verify(cardService, never()).getCardBalance(any());
    }

    @Test
    void transferWithValidData() {
        Long userId = 1L;
        TransferRequestDTO transferRequest = new TransferRequestDTO();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(new BigDecimal("500.00"));

        when(userDetails.getUserId()).thenReturn(userId);
        doNothing().when(transferService).transferBetweenCards(1L, 2L, userId, new BigDecimal("500.00"));
        ResponseEntity<Void> result = userCardController.transfer(transferRequest, userDetails);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
        verify(transferService).transferBetweenCards(1L, 2L, userId, new BigDecimal("500.00"));
    }

    @Test
    void transferWithTransferException() {
        Long userId = 1L;
        TransferRequestDTO transferRequest = new TransferRequestDTO();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(new BigDecimal("500.00"));

        when(userDetails.getUserId()).thenReturn(userId);
        doThrow(new RuntimeException("Transfer failed",null))
                .when(transferService).transferBetweenCards(1L, 2L, userId, new BigDecimal("500.00"));

        assertThrows(RuntimeException.class, () ->
                userCardController.transfer(transferRequest, userDetails));

        verify(transferService).transferBetweenCards(1L, 2L, userId, new BigDecimal("500.00"));
    }

    @Test
    void transferWithInsufficientFunds()  {
        Long userId = 1L;
        TransferRequestDTO transferRequest = new TransferRequestDTO();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(new BigDecimal("5000.00"));

        when(userDetails.getUserId()).thenReturn(userId);
        doThrow(new IllegalStateException("Insufficient funds"))
                .when(transferService).transferBetweenCards(1L, 2L, userId, new BigDecimal("5000.00"));

        assertThrows(IllegalStateException.class, () ->
                userCardController.transfer(transferRequest, userDetails));

        verify(transferService).transferBetweenCards(1L, 2L, userId, new BigDecimal("5000.00"));
    }

    @Test
    void transferWithInactiveCard()  {
        Long userId = 1L;
        TransferRequestDTO transferRequest = new TransferRequestDTO();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(userDetails.getUserId()).thenReturn(userId);
        doThrow(new IllegalStateException("Card is not active"))
                .when(transferService).transferBetweenCards(1L, 2L, userId, new BigDecimal("100.00"));

        assertThrows(IllegalStateException.class, () ->
                userCardController.transfer(transferRequest, userDetails));

        verify(transferService).transferBetweenCards(1L, 2L, userId, new BigDecimal("100.00"));
    }

    @Test
    void getUserCardsWithEmptyResult() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> emptyPage = Page.empty();

        when(userDetails.getUserId()).thenReturn(userId);
        when(cardService.getUserCards(userId, pageable)).thenReturn(emptyPage);

        ResponseEntity<Page<CardResponseDTO>> result = userCardController.getUserCards(userDetails, pageable);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
        verify(cardService).getUserCards(userId, pageable);
    }
}
