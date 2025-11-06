package com.example.bankcards.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * для реализации перевода между картами??
 */
public class Transaction {
    private Long id;
    private Card fromCard;
    private Card toCard;
    private BigDecimal amount;
    private LocalDateTime timeStamp;
    private TransactionStatus transactionStatus;
}
