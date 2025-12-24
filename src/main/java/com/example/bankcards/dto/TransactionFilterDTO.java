package com.example.bankcards.dto;

import com.example.bankcards.entity.TransactionStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionFilterDTO {
    private TransactionStatus status;
    @Positive
    private BigDecimal minAmount;
    @Positive
    private BigDecimal maxAmount;
    @PastOrPresent
    private LocalDateTime startDate;
    @FutureOrPresent
    private LocalDateTime endDate;

    @AssertTrue(message = "endDate must be after startDate")
    public boolean isEndDateValid() {
        return endDate == null || startDate == null || endDate.isAfter(startDate);
    }
}
