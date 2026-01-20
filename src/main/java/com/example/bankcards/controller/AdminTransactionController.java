package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionFilterDTO;
import com.example.bankcards.dto.TransactionResponseDTO;
import com.example.bankcards.constants.Role;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.util.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
/**
 * для admin(все транзакции, фильтр по карте, фильтр по сумме, аналитика)
 */
@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminTransactionController {
    private final TransactionService transactionService;

    @GetMapping//все транзакции всех пользователей с фильтрами
    public ResponseEntity<Page<TransactionResponseDTO>> getAllTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute @Valid TransactionFilterDTO filterDTO,
            @RequestParam(required = false) Long userId,//для админа, фильтровать пользователей
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {


        Long authUserId = userDetails.getUserId();//id авторизованного пользователя
        Role role = userDetails.getRole();
        Long targetUserId = (role == Role.ADMIN && userId != null) ? userId : authUserId;
        Pageable pageable = PageRequest.of(page,size);
        Page<TransactionResponseDTO> result = transactionService.getFilteredTransaction(targetUserId,filterDTO,pageable);
        return ResponseEntity.ok(result);
    }
}
