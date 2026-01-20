package com.example.bankcards.dto;

import com.example.bankcards.constants.CardStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * для передачи данных карты клиенту
 */
@Data
public class CardResponseDTO {
    private Long id;
    private Long userId;
    private String maskedNumber;
    private LocalDate expireDate;
    private CardStatus cardStatus;
    private BigDecimal balance;

}
