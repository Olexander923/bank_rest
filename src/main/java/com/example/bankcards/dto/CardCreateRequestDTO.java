package com.example.bankcards.dto;

import lombok.Getter;
import lombok.Setter;

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
}
