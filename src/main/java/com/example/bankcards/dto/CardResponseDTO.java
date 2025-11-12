package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

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
