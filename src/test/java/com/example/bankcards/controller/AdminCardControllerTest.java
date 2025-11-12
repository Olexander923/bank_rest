//package com.example.bankcards.controller;
//
//import com.example.bankcards.dto.CardCreateRequestDTO;
//import com.example.bankcards.dto.CardResponseDTO;
//import com.example.bankcards.entity.Card;
//import com.example.bankcards.entity.CardStatus;
//import com.example.bankcards.security.CustomUserDetailService;
//import com.example.bankcards.security.JwtUtils;
//import com.example.bankcards.security.SecurityConfig;
//import com.example.bankcards.service.CardService;
//import com.example.bankcards.util.CardMapper;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//import static org.springframework.http.MediaType.APPLICATION_JSON;
//import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@DisplayName("тест эндпоинтов для операций в роли админа,security для тестов отключаем")
//@WebMvcTest(AdminCardController.class)
//@Import(SecurityConfig.class) // Подключаем вашу основную Security конфигурацию
//class AdminCardControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private CardService cardService;
//
//    @MockitoBean
//    private CardMapper cardMapper;
//
//    // Вспомогательные данные
//    private final Long CARD_ID = 1L;
//    private final Long USER_ID = 100L;
//
//    // ------------------- createCard -------------------
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void createCard_WithValidRequest_ReturnsOk() throws Exception {
//        CardCreateRequestDTO request = new CardCreateRequestDTO();
//        request.setUserId(USER_ID);
//        request.setCardNumber("1234567890123456");
//        request.setOwner("John Doe");
//        request.setExpiryDate("12/28");
//
//        Card mockCard = new Card();
//        mockCard.setId(CARD_ID);
//        mockCard.setUserId(USER_ID);
//
//        CardResponseDTO mockResponse = new CardResponseDTO();
//        mockResponse.setId(CARD_ID);
//        mockResponse.setUserId(USER_ID);
//        mockResponse.setMaskedCardNumber("**** **** **** 3456");
//
//        when(cardService.createCard(any(CardCreateRequestDTO.class), eq(USER_ID))).thenReturn(mockCard);
//        when(cardMapper.toDTO(mockCard)).thenReturn(mockResponse);
//
//        mockMvc.perform(post("/api/admin/cards")
//                        .contentType(APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(CARD_ID))
//                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 3456"));
//
//        verify(cardService).createCard(any(CardCreateRequestDTO.class), eq(USER_ID));
//        verify(cardMapper).toDTO(mockCard);
//    }
//
//    // ------------------- deleteCard -------------------
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void deleteCard_ExistingCard_ReturnsNoContent() throws Exception {
//        doNothing().when(cardService).deleteCard(CARD_ID);
//
//        mockMvc.perform(delete("/api/admin/cards/{cardId}", CARD_ID))
//                .andExpect(status().isNoContent());
//
//        verify(cardService).deleteCard(CARD_ID);
//    }
//
//    // ------------------- blockCard -------------------
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void blockCard_ExistingCard_ReturnsOk() throws Exception {
//        Card mockCard = new Card();
//        mockCard.setId(CARD_ID);
//        mockCard.setStatus(CardStatus.BLOCKED);
//
//        CardResponseDTO mockResponse = new CardResponseDTO();
//        mockResponse.setId(CARD_ID);
//        mockResponse.setStatus(CardStatus.BLOCKED);
//
//        when(cardService.blockCard(CARD_ID)).thenReturn(mockCard);
//        when(cardMapper.toDTO(mockCard)).thenReturn(mockResponse);
//
//        mockMvc.perform(post("/api/admin/cards/{cardId}/block", CARD_ID))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("BLOCKED"));
//
//        verify(cardService).blockCard(CARD_ID);
//        verify(cardMapper).toDTO(mockCard);
//    }
//
//    // ------------------- activateCard -------------------
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void activateCard_ExistingCard_ReturnsOk() throws Exception {
//        Card mockCard = new Card();
//        mockCard.setId(CARD_ID);
//        mockCard.setStatus(CardStatus.ACTIVE);
//
//        CardResponseDTO mockResponse = new CardResponseDTO();
//        mockResponse.setId(CARD_ID);
//        mockResponse.setStatus(CardStatus.ACTIVE);
//
//        when(cardService.activateCard(CARD_ID)).thenReturn(mockCard);
//        when(cardMapper.toDTO(mockCard)).thenReturn(mockResponse);
//
//        mockMvc.perform(patch("/api/admin/cards/{cardId}/activate", CARD_ID))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("ACTIVE"));
//
//        verify(cardService).activateCard(CARD_ID);
//        verify(cardMapper).toDTO(mockCard);
//    }
//
//    // ------------------- getAllCards -------------------
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void getAllCards_ReturnsListOfCards() throws Exception {
//        Card card1 = new Card();
//        card1.setId(1L);
//        Card card2 = new Card();
//        card2.setId(2L);
//
//        CardResponseDTO dto1 = new CardResponseDTO();
//        dto1.setId(1L);
//        CardResponseDTO dto2 = new CardResponseDTO();
//        dto2.setId(2L);
//
//        when(cardService.getAllCards()).thenReturn(List.of(card1, card2));
//        when(cardMapper.toDTO(card1)).thenReturn(dto1);
//        when(cardMapper.toDTO(card2)).thenReturn(dto2);
//
//        mockMvc.perform(get("/api/admin/cards"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].id").value(1))
//                .andExpect(jsonPath("$[1].id").value(2));
//
//        verify(cardService).getAllCards();
//    }
//
//    // ------------------- Unauthorized access -------------------
//    @Test
//    void getAllCards_WithoutAdminRole_ReturnsForbidden() throws Exception {
//        mockMvc.perform(get("/api/admin/cards"))
//                .andExpect(status().isForbidden());
//    }
//}