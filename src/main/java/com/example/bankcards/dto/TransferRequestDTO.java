package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * для запроса на перевод
 */
@Getter
@Setter
public class TransferRequestDTO {
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
}
