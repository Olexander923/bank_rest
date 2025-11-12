package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * для запроса на создание карты от администратора
 */
@Getter
@Setter
public class CardCreateRequestDTO {
    private Long userId;
    private String cardNumber;
    private LocalDate expireDate;
    private CardStatus cardStatus;   // ← добавить
    private BigDecimal balance;
}
