package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequestDTO;
import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.service.CardService;

import com.example.bankcards.util.CardMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@WebMvcTest(AdminCardController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@WithMockUser(roles = "ADMIN")
public class AdminCardControllerTestsWeb {
    @MockitoBean private CardService cardService;
    @MockitoBean private CardMapper cardMapper;
    @Autowired private MockMvc mockMvc;

    @Test
    void createCard_return200() throws Exception {
        Long userId = 1L;
        User user = new User("testuser", "password", "test@example.com", Role.USER);
        user.setId(userId);

        Card mockCard = new Card(
                "encrypted_number",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                user
        );
        mockCard.setId(1L);

        CardResponseDTO mockResponseDTO = new CardResponseDTO();
        mockResponseDTO.setId(1L);
        mockResponseDTO.setUserId(userId);
        mockResponseDTO.setMaskedNumber("400000******0002");
        mockResponseDTO.setExpireDate(LocalDate.of(2028, 12, 31));
        mockResponseDTO.setCardStatus(CardStatus.ACTIVE);
        mockResponseDTO.setBalance(new BigDecimal("5000.00"));

        when(cardService.createCard(any(CardCreateRequestDTO.class), eq(userId)))
                .thenReturn(mockCard);
        when(cardMapper.toDTO(any())).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            System.out.println("Card ID in mapper: " + card.getId());
            CardResponseDTO dto = new CardResponseDTO();
            dto.setId(card.getId());
            dto.setUserId(card.getUser().getId());
            dto.setMaskedNumber("400000******0002");
            dto.setBalance(card.getBalance());
            dto.setCardStatus(card.getCardStatus());
            dto.setExpireDate(card.getExpireDate());
            return dto;
        });

        mockMvc.perform(post("/api/admin/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                "cardNumber": "4000000000000002",
                "expireDate": "2028-12-31",
                "balance": 5000.00,
                "userId": 1,
                "cardStatus": "ACTIVE"
            }
            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        verify(cardService, times(1))
                .createCard(any(CardCreateRequestDTO.class), eq(userId));
    }

    @Test
    void createCard_return400_invalidCardNumber() throws Exception {
        Long userId = 1L;
        User user = new User("testuser", "password", "test@example.com", Role.USER);
        user.setId(userId);

        Card mockCard = new Card(
                "encrypted_number",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                user
        );
        mockCard.setId(1L);

        CardResponseDTO mockResponseDTO = new CardResponseDTO();
        mockResponseDTO.setId(1L);
        mockResponseDTO.setUserId(userId);
        mockResponseDTO.setMaskedNumber("400000******0002");
        mockResponseDTO.setExpireDate(LocalDate.of(2028, 12, 31));
        mockResponseDTO.setCardStatus(CardStatus.ACTIVE);
        mockResponseDTO.setBalance(new BigDecimal("5000.00"));

        when(cardService.createCard(any(CardCreateRequestDTO.class), eq(1L)))
                .thenThrow(new ValidationException("Invalid card number!"));


        mockMvc.perform(post("/api/admin/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                "cardNumber": "4000000000003212",
                "expireDate": "2028-12-31",
                "balance": 5000.00,
                "userId": 1,
                "cardStatus": "ACTIVE"
            }
            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid card number!"));
        verify(cardService, times(1))
                .createCard(any(CardCreateRequestDTO.class), eq(1L));
    }

    @Test
    @WithMockUser("ADMIN")
    void createCardForNotExistingUser_return404() throws Exception {
        Long userId = 999L;

        when(cardService.createCard(any(CardCreateRequestDTO.class), eq(userId)))
                .thenThrow(new UserNotFoundException("User with this id not found"));

        mockMvc.perform(post("/api/admin/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                "cardNumber": "4000000000003212",
                "expireDate": "2028-12-31",
                "balance": 5000.00,
                "userId": 999,
                "cardStatus": "ACTIVE"
            }
            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with this id not found"));
        verify(cardService, times(1)).
                createCard(any(CardCreateRequestDTO.class), eq(userId));

    }

        @Test
        void createCardWithNegativeBalance_return400() throws Exception {
            Long userId = 1L;

            when(cardService.createCard(any(CardCreateRequestDTO.class), eq(userId)))
                    .thenThrow(new ValidationException("Card balance cannot be negative!"));

            mockMvc.perform(post("/api/admin/cards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
            {
                "cardNumber": "4000000000000002",
                "expireDate": "2028-12-31",
                "balance": -5000.00,
                "userId": 1,
                "cardStatus": "ACTIVE"
            }
            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Card balance cannot be negative!"));
            verify(cardService, times(1))
                    .createCard(any(CardCreateRequestDTO.class), eq(userId));
        }

        @Test
        void createCardWithExpiredDate_return400() throws Exception {
            Long userId = 1L;

            when(cardService.createCard(any(CardCreateRequestDTO.class), eq(userId)))
                    .thenThrow(new ValidationException("Card is expired!"));

            mockMvc.perform(post("/api/admin/cards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
            {
                "cardNumber": "4000000000003212",
                "expireDate": "2024-12-31",
                "balance": 5000.00,
                "userId": 1,
                "cardStatus": "ACTIVE"
            }
            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Card is expired!"));
            verify(cardService, times(1)).createCard(any(CardCreateRequestDTO.class), eq(userId));
        }

        @Test
        void createCardWithWrongExpireDate_return400() throws Exception {
            mockMvc.perform(post("/api/admin/cards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
            {
                "cardNumber": "4000000000003212",
                "expireDate": "2028/12/31",
                "balance": 5000.00,
                "userId": 1,
                "cardStatus": "ACTIVE"
            }
            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid date format. Use YYYY-MM-DD."));
        }

        @Test
        void createCardWithoutJsonField() throws Exception {
            mockMvc.perform(post("/api/admin/cards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
            {
                "cardNumber": "4000000000003212",
                "expireDate": "2028-12-31",
                
                "userId": 1,
                "cardStatus": "ACTIVE"
            }
            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Balance is required"));
        }

        @Test
        void createCard_tooLongCardNumber_return400() throws Exception {
            mockMvc.perform(post("/api/admin/cards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
            {
                "cardNumber": "400000000000000002",
                "expireDate": "2028-12-31",
                "balance": 5000.00,
                "userId": 1,
                "cardStatus": "ACTIVE"
            }
            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("The card number must be exactly 16 digits long!"));
        }

    @Test
    void createCardWithDuplication_return409() throws Exception {
        Long userId = 1L;
        when(cardService.createCard(any(CardCreateRequestDTO.class),eq(userId)))
                .thenThrow(new CardAlreadyExistException("Card with this number already exist!"));

        mockMvc.perform(post("/api/admin/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                "cardNumber": "4000000000000002",
                "expireDate": "2024-12-31",
                "balance": 5000.00,
                "userId": 1,
                "cardStatus": "ACTIVE"
            }
            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Card with this number already exist!"));
    }

    @DisplayName("method 'deleteCard' tests")
    @Test
    void deleteCard_return200() throws Exception {
        mockMvc.perform(delete("/api/admin/cards/1"))
                .andExpect(status().isNoContent());
    }

    @Test //проверка что api отвечает не неправильный формат id
    void deleteCard_API_return400() throws Exception{
        mockMvc.perform(delete("/api/admin/cards/gh"))
                .andExpect(status().isBadRequest());
    }

    @Test//проверка что id нет в бд
    void deleteCard_return404() throws Exception {
        Long cardId = 999L;
        doThrow(new CardNotFoundException("Card not found!"))
                .when(cardService)
                .deleteCard(cardId);
       mockMvc.perform(delete("/api/admin/cards/999"))
               .andExpect(status().isNotFound());
    }

    @DisplayName("method 'blockCard' tests")
    @Test
    void blockCard_return200() throws Exception {
        Long cardId = 1L;
        Long userId = 1L;
        User user = new User("testuser", "password", "test@example.com", Role.USER);
        user.setId(userId);

        Card mockCard = new Card(
                "encrypted_number",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                user
        );
        mockCard.setId(1L);
        when(cardService.blockCard(cardId)).thenReturn(mockCard);
        mockMvc.perform(post("/api/admin/cards/1/block"))
                .andExpect(status().isOk());
    }

}
