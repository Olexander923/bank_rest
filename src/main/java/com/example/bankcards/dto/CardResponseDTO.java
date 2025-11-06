package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * для передачи данных карты клиенту
 */
@Getter
@Setter
public class CardResponseDTO {
    private Long id;
    private Long userId;
    private String maskedNumber;
    private LocalDate expireDate;
    private CardStatus cardStatus;
    private BigDecimal balance;


}
