package com.example.bankcards.dto;

import com.example.bankcards.constants.TransactionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * для передачи данных транзакций
 */
@Data
public class TransactionResponseDTO {
    private Long id;
    private String fromMaskedCard;
    private String toMaskedCard;
    private BigDecimal amount;
    private LocalDateTime timeStamp;
    private TransactionStatus transactionStatus;
}
