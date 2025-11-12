package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.UserNameAlreadyExistException;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCardController.class)
@Import(TestSecurityConfig.class)
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private CardMapper cardMapper;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ===== CREATE CARD =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_WithAdminRole_ShouldReturnCard() throws Exception {
        // Given
        String createRequestJson = """
        {
            "userId": 1,
            "cardNumber": "1234567890123456",
            "ownerName": "John Doe"
        }
        """;

        Card card = mock(Card.class);
        CardResponseDTO responseDTO = new CardResponseDTO(); // ← ИСПОЛЬЗУЕМ РЕАЛЬНЫЙ ОБЪЕКТ

        // Настраиваем реальный объект вместо мока
        responseDTO.setId(1L);
        responseDTO.setMaskedNumber("**** **** **** 3456");
        responseDTO.setCardStatus(CardStatus.ACTIVE);
        responseDTO.setUserId(1L);
        responseDTO.setBalance(BigDecimal.ZERO);
        responseDTO.setExpireDate(LocalDate.now().plusYears(3));

        when(cardService.createCard(any(CardCreateRequestDTO.class), anyLong())).thenReturn(card);
        when(cardMapper.toDTO(card)).thenReturn(responseDTO); // ← Возвращаем реальный объект

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/admin/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        // Отладочная печать
        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Response body: " + responseBody);

        // Проверяем что ответ не пустой
        assertThat(responseBody).isNotEmpty();

        // Затем проверяем JSON path
        mockMvc.perform(post("/api/admin/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 3456"))
                .andExpect(jsonPath("$.cardStatus").value("ACTIVE"));

        verify(cardService).createCard(any(CardCreateRequestDTO.class), eq(1L));
        verify(cardMapper).toDTO(card);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCard_WithUserRole_ShouldReturnForbidden() throws Exception {
        // Given
        String createRequestJson = "{\"userId\": 1}";

        // When & Then
        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson))
                .andExpect(status().isForbidden());

        verify(cardService, never()).createCard(any(), anyLong());
    }

    // ===== DELETE CARD =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_WithAdminRole_ShouldReturnNoContent() throws Exception {
        // Given
        Long cardId = 1L;
        doNothing().when(cardService).deleteCard(cardId);

        // When & Then
        mockMvc.perform(delete("/api/admin/cards/{cardId}", cardId))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(cardId);
    }

    // ===== BLOCK CARD =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_WithAdminRole_ShouldReturnBlockedCard() throws Exception {
        // Given
        Long cardId = 1L;
        Card blockedCard = mock(Card.class);
        CardResponseDTO responseDTO = mock(CardResponseDTO.class);

        when(cardService.blockCard(cardId)).thenReturn(blockedCard);
        when(cardMapper.toDTO(blockedCard)).thenReturn(responseDTO);
        when(responseDTO.getId()).thenReturn(cardId);
        when(responseDTO.getCardStatus()).thenReturn(CardStatus.BLOCKED);

        // When & Then
        mockMvc.perform(post("/api/admin/cards/{cardId}/block", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(cardService).blockCard(cardId);
        verify(cardMapper).toDTO(blockedCard);
    }

    // ===== ACTIVATE CARD =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_WithAdminRole_ShouldReturnActivatedCard() throws Exception {
        // Given
        Long cardId = 1L;
        Card activatedCard = mock(Card.class);
        CardResponseDTO responseDTO = mock(CardResponseDTO.class);

        when(cardService.activateCard(cardId)).thenReturn(activatedCard);
        when(cardMapper.toDTO(activatedCard)).thenReturn(responseDTO);
        when(responseDTO.getId()).thenReturn(cardId);
        when(responseDTO.getCardStatus()).thenReturn(CardStatus.ACTIVE);

        // When & Then
        mockMvc.perform(patch("/api/admin/cards/{cardId}/activate", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(cardService).activateCard(cardId);
        verify(cardMapper).toDTO(activatedCard);
    }

    // ===== GET ALL CARDS =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_WithAdminRole_ShouldReturnAllCards() throws Exception {
        // Given
        Card card1 = mock(Card.class);
        Card card2 = mock(Card.class);

        CardResponseDTO dto1 = mock(CardResponseDTO.class);
        CardResponseDTO dto2 = mock(CardResponseDTO.class);

        when(cardService.getAllCards()).thenReturn(List.of(card1, card2));
        when(cardMapper.toDTO(card1)).thenReturn(dto1);
        when(cardMapper.toDTO(card2)).thenReturn(dto2);
        when(dto1.getId()).thenReturn(1L);
        when(dto2.getId()).thenReturn(2L);

        // When & Then
        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(cardService).getAllCards();
        verify(cardMapper, times(2)).toDTO(any(Card.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCards_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isForbidden());

        verify(cardService, never()).getAllCards();
    }

    // ===== ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_WithMinimalData_ShouldWork() throws Exception {
        // Given - минимальные данные
        String minimalRequestJson = "{\"userId\": 1}";

        Card card = mock(Card.class);
        CardResponseDTO responseDTO = mock(CardResponseDTO.class);

        when(cardService.createCard(any(CardCreateRequestDTO.class), anyLong())).thenReturn(card);
        when(cardMapper.toDTO(card)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(minimalRequestJson))
                .andExpect(status().isOk());

        verify(cardService).createCard(any(CardCreateRequestDTO.class), eq(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_NonExistentCard_ShouldCallService() throws Exception {
        // Given
        Long nonExistentCardId = 999L;
        Card card = mock(Card.class);
        CardResponseDTO responseDTO = mock(CardResponseDTO.class);

        when(cardService.blockCard(nonExistentCardId)).thenReturn(card);
        when(cardMapper.toDTO(card)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/admin/cards/{cardId}/block", nonExistentCardId))
                .andExpect(status().isOk());

        verify(cardService).blockCard(nonExistentCardId);
    }
}
