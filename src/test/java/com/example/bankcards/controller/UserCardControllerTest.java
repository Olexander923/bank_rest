package com.example.bankcards.controller;

import com.example.bankcards.controller.test_security_configs.AdminUserControllerTestConfig;
import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.security.CustomUserDetailService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtUtils;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.util.CardMapper;
import com.example.bankcards.util.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserCardController.class)
@Import({AdminUserControllerTestConfig.class, GlobalExceptionHandler.class})
public class UserCardControllerTest {

    @MockitoBean
    private CardService cardService;
    @MockitoBean
    private TransferService transferService;
    @MockitoBean
    private CardMapper cardMapper;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private CustomUserDetailService customUserDetailService;
    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetails userDetails;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws ServletException, IOException {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
        CustomUserDetails customUserDetails = new CustomUserDetails(
                1L, "testuser", "ValidPass1@",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                "password",
                customUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @DisplayName("method 'getUserCards' tests")
    @Test
    void getUserCards_return200() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card1 = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                user
        );
        Card card2 = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("3000.00"),
                user
        );

        Page<Card> cardPage = new PageImpl<>(List.of(card2, card1));

        when(cardService.getUserCards(eq(1L), any(Pageable.class))).thenReturn(cardPage);

        CardResponseDTO dto1 = new CardResponseDTO();
        dto1.setId(1L);
        CardResponseDTO dto2 = new CardResponseDTO();
        dto2.setId(2L);

        when(cardMapper.toDTO(any(Card.class))).thenReturn(dto1, dto2);

        mockMvc.perform(get("/api/user/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        //проверка, что сервис был вызван с правильным userId
        verify(cardService).getUserCards(eq(1L), any(Pageable.class));
    }

    @Test
    void getUserCards_return400() throws Exception {
        Page<Card> emptyPage = new PageImpl<>(Collections.emptyList());
        when(cardService.getUserCards(anyLong(), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/user/cards")
                        .param("page", "-1")  //
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardBalance_return200() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                user
        );
        when(cardService.getCardBalance(eq(2L))).thenReturn(card.getBalance());
        when(cardService.getCardById(2L)).thenReturn(Optional.of(card));
        mockMvc.perform(get("/api/user/cards/2/balance"))
                .andExpect(status().isOk());
    }

    @Test
    void getCardBalance_InvalidCardId_return404() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                user
        );
        when(cardService.getCardBalance(eq(999L))).thenReturn(card.getBalance());
        when(cardService.getCardById(999L))
                .thenThrow(new CardNotFoundException("Card with this id not found"));
        mockMvc.perform(get("/api/user/cards/999/balance"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCardBalance_BlockedCard_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card blockedCard = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.BLOCKED,
                new BigDecimal("5000.00"),
                user
        );
        when(cardService.getCardBalance(1L))
                .thenThrow(new CardBlockedException("Card blocked"));
        when(cardService.getCardById(any())).thenReturn(Optional.of(blockedCard));
        mockMvc.perform(get("/api/user/cards/1/balance"))
                .andExpect(status().isBadRequest());

    }

    @Test
    void getCardBalance_API_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                user
        );
        when(cardService.getCardBalance(eq(1L))).thenReturn(card.getBalance());
        when(cardService.getCardById(1L)).thenReturn(Optional.of(card));

        mockMvc.perform(get("/api/user/cards/tweqyqw/balance"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardBalance_negativeCardId_return404() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                user
        );
        when(cardService.getCardBalance(eq(1L))).thenReturn(card.getBalance());
        when(cardService.getCardById(-10L))
                .thenThrow(new CardNotFoundException("Card with this id not found"));
        mockMvc.perform(get("/api/user/cards/-10/balance"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCardBalanceWithExpiredDate_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2021, 12, 31),
                CardStatus.EXPIRED,
                new BigDecimal("5000.00"),
                user
        );
        when(cardService.getCardBalance(eq(1L))).thenThrow(new ValidationException("Card is expired"));
        when(cardService.getCardById(1L)).thenReturn(Optional.of(card));
        mockMvc.perform(get("/api/user/cards/1/balance"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("method 'transfer' tests")
    @Test
    void transfer_return200() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2021, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                user
        );
        TransferRequestDTO requestDTO = new TransferRequestDTO();
        requestDTO.setFromCardId(1L);
        requestDTO.setToCardId(2L);
        requestDTO.setAmount(new BigDecimal("3500"));

        doNothing().when(transferService).transferBetweenCards(eq(1L), eq(2L), eq(1L), eq(new BigDecimal("3500")));
        when(cardService.getCardById(1L)).thenReturn(Optional.of(card));
        mockMvc.perform(post("/api/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void transferInsufficientFunds_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2021, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                user
        );
        TransferRequestDTO requestDTO = new TransferRequestDTO();
        requestDTO.setFromCardId(1L);
        requestDTO.setToCardId(2L);
        requestDTO.setAmount(new BigDecimal("3500"));

        doThrow(new InsufficientFundsException("Insufficient funds."))
                .when(transferService).transferBetweenCards(eq(1L), eq(2L), eq(1L), eq(new BigDecimal("3500")));
        when(cardService.getCardById(1L)).thenReturn(Optional.of(card));
        mockMvc.perform(post("/api/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferBlockedCard_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card blockedCard = new Card(
                "400000******0002",
                LocalDate.of(2021, 12, 31),
                CardStatus.BLOCKED,
                new BigDecimal("5000.00"),
                user
        );
        TransferRequestDTO requestDTO = new TransferRequestDTO();
        requestDTO.setFromCardId(1L);
        requestDTO.setToCardId(2L);
        requestDTO.setAmount(new BigDecimal("3500"));

        doThrow(new CardBlockedException("Card blocked"))
                .when(transferService).transferBetweenCards(eq(1L), eq(2L), eq(1L), eq(new BigDecimal("3500")));
        when(cardService.getCardById(1L)).thenReturn(Optional.of(blockedCard));
        mockMvc.perform(post("/api/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferWithExpiredDate_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card expiredCard = new Card(
                "400000******0002",
                LocalDate.of(2021, 12, 31),
                CardStatus.EXPIRED,
                new BigDecimal("5000.00"),
                user
        );
        TransferRequestDTO requestDTO = new TransferRequestDTO();
        requestDTO.setFromCardId(1L);
        requestDTO.setToCardId(2L);
        requestDTO.setAmount(new BigDecimal("3500"));

        doThrow(new CardExpiredException("Card is expired"))
                .when(transferService).transferBetweenCards(eq(1L), eq(2L), eq(1L), eq(new BigDecimal("3500")));
        when(cardService.getCardById(1L)).thenReturn(Optional.of(expiredCard));
        mockMvc.perform(post("/api/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferWithNegativeBalance_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2021, 12, 31),
                CardStatus.EXPIRED,
                new BigDecimal("-5000.00"),
                user
        );
        TransferRequestDTO requestDTO = new TransferRequestDTO();
        requestDTO.setFromCardId(1L);
        requestDTO.setToCardId(2L);
        requestDTO.setAmount(new BigDecimal("3500"));

        doThrow(new ValidationException("Card balance cannot be negative!"))
                .when(transferService).transferBetweenCards(eq(1L), eq(2L), eq(1L), eq(new BigDecimal("3500")));
        when(cardService.getCardById(1L)).thenReturn(Optional.of(card));
        mockMvc.perform(post("/api/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferNotEnoughMoney_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2021, 12, 31),
                CardStatus.EXPIRED,
                new BigDecimal("0.00"),
                user
        );
        TransferRequestDTO requestDTO = new TransferRequestDTO();
        requestDTO.setFromCardId(1L);
        requestDTO.setToCardId(2L);
        requestDTO.setAmount(new BigDecimal("3500"));

        doThrow(new InsufficientFundsException("Not enough money!"))
                .when(transferService).transferBetweenCards(eq(1L), eq(2L), eq(1L), eq(new BigDecimal("3500")));
        when(cardService.getCardById(1L)).thenReturn(Optional.of(card));
        mockMvc.perform(post("/api/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferSameCardTransfer_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2021, 12, 31),
                CardStatus.EXPIRED,
                new BigDecimal("5000.00"),
                user
        );
        TransferRequestDTO requestDTO = new TransferRequestDTO();
        requestDTO.setFromCardId(1L);
        requestDTO.setToCardId(1L);
        requestDTO.setAmount(new BigDecimal("3500"));

        doThrow(new IllegalStateException("Can not transfer to the same card!"))
                .when(transferService).transferBetweenCards(eq(1L), eq(1L), eq(1L), eq(new BigDecimal("3500")));
        when(cardService.getCardById(1L)).thenReturn(Optional.of(card));
        mockMvc.perform(post("/api/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferAmountExceedsLimit_return400() throws Exception {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        user.setId(1L);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2021, 12, 31),
                CardStatus.EXPIRED,
                new BigDecimal("150000000000.00"),
                user
        );
        TransferRequestDTO requestDTO = new TransferRequestDTO();
        requestDTO.setFromCardId(1L);
        requestDTO.setToCardId(2L);
        requestDTO.setAmount(new BigDecimal("100000000.00"));

        doThrow(new IllegalStateException("Transfer amount exceeds maximum limit."))
                .when(transferService).transferBetweenCards(eq(1L), eq(2L), eq(1L), eq(new BigDecimal("100000000.00")));
        when(cardService.getCardById(1L)).thenReturn(Optional.of(card));
        mockMvc.perform(post("/api/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }


}
