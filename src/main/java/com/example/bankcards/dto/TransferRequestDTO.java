package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * для запроса на перевод
 */
@Data
public class TransferRequestDTO {
    @NotNull
    private Long fromCardId;

    @NotNull
    private Long toCardId;

    @NotNull @Positive
    private BigDecimal amount;
}
