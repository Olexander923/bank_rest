package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * для передачи ошибок
 */
@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    private String message;

}
