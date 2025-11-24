package com.example.bankcards.security;

import com.example.bankcards.controller.AdminCardController;
import com.example.bankcards.controller.TestSecurityConfig;
import com.example.bankcards.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCardController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})//todo заменить на новый конфиг!
@WithMockUser(roles = "ADMIN")
public class SecurityTestOnly {
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
