package com.example.bankcards.security;

import com.example.bankcards.controller.AdminCardController;
import com.example.bankcards.controller.test_security_configs.AdminUserControllerTestConfig;
import com.example.bankcards.controller.test_security_configs.SecurityTestOnlyConfig;
import com.example.bankcards.controller.test_security_configs.TestSecurityConfig;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCardController.class)
@Import({SecurityTestOnlyConfig.class, GlobalExceptionHandler.class})
public class SecurityTestOnly {

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private CardMapper cardMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "USER")
    void createCardWithWrongRole_return403() throws Exception {
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
                .andExpect(status().isForbidden());

    }
}
