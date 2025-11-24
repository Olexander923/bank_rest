package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
