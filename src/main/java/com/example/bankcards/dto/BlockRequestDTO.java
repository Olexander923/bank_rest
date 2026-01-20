package com.example.bankcards.dto;

import com.example.bankcards.constants.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BlockRequestDTO {
    @NotNull
    private Long id;
    @NotNull
    private Long cardId;
    @NotNull
    private Long userId;
    @NotNull
    private LocalDateTime requestDate;
    @NotNull
    private RequestStatus requestStatus;
    @NotNull
    private String maskedNumber;
}
