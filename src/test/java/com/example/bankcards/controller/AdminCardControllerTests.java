package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.CardCreateRequestDTO;
import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.CustomUserDetailService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtUtils;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardEncryptionUtil;
import com.example.bankcards.util.CardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AdminCardControllerTests {

    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    @Mock
    private CardMapper cardMapper;

    @BeforeEach
    void setUp() {
        // Создаем MockMvc вручную для AdminCardController
        AdminCardController controller = new AdminCardController(cardService, cardMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }

    @Test
    void createCard_return200() throws Exception {
        // 1. Подготовка данных
        Long userId = 1L;

        String requestBody = """
            {
                "cardNumber": "4000000000000002",
                "expireDate": "2028-12-31",
                "balance": 5000.00,
                "userId": 1,
                "cardStatus": "ACTIVE"
            }
            """;

        // 2. Мок карты
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

        // 3. Мок ответа
        CardResponseDTO mockResponseDTO = new CardResponseDTO();
        mockResponseDTO.setId(1L);
        mockResponseDTO.setUserId(userId);
        mockResponseDTO.setMaskedNumber("400000******0002");
        mockResponseDTO.setExpireDate(LocalDate.of(2028, 12, 31));
        mockResponseDTO.setCardStatus(CardStatus.ACTIVE);
        mockResponseDTO.setBalance(new BigDecimal("5000.00"));

        // 4. Настройка моков
        when(cardService.createCard(any(CardCreateRequestDTO.class), eq(userId)))
                .thenReturn(mockCard);
        when(cardMapper.toDTO(mockCard))
                .thenReturn(mockResponseDTO);

        // 5. Выполнение запроса БЕЗ security
        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedNumber").value("400000******0002"))
                .andExpect(jsonPath("$.userId").value(1));

        // 6. Проверка вызовов
        verify(cardService, times(1)).createCard(any(CardCreateRequestDTO.class), eq(userId));
        verify(cardMapper, times(1)).toDTO(mockCard);
    }
}