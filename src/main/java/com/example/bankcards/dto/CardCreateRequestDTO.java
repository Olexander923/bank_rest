package com.example.bankcards.dto;

import com.example.bankcards.constants.CardStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * для запроса на создание карты от администратора
 */
@Data
@NoArgsConstructor
public class CardCreateRequestDTO {
    @NotNull
    private Long userId;
    @NotNull
    @Size(min = 16,max = 16,
            message = "The card number must be exactly 16 digits long!")
    private String cardNumber;
    @NotNull
    private LocalDate expireDate;
    @NotNull
    private CardStatus cardStatus;
    @NotNull(message = "Balance is required")
    private BigDecimal balance;
}
