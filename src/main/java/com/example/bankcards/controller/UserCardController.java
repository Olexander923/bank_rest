package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.TransferFailedException;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardMapper;
import com.example.bankcards.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * для пользователя(мои карты,переводы, баланс)
 */
@RestController
@RequestMapping("/api/user/cards")
@RequiredArgsConstructor
public class UserCardController {
    private final CardService cardService;
    private final TransferService transferService;
    private final CardMapper cardMapper;

    @GetMapping
    //для получения всех карт пользователя
    public ResponseEntity<Page<CardResponseDTO>> getUserCards(
            @AuthenticationPrincipal CustomUserDetails userDetails, Pageable pageable) {
        Long userId = userDetails.getUserId();
        Page<Card> cards = cardService.getUserCards(userId, pageable);
        Page<CardResponseDTO> dtoPage = cards.map(cardMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }


    @GetMapping("{cardId}/balance")
    public ResponseEntity<BigDecimal> getCardBalance(
            @PathVariable Long cardId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Card card = cardService.getCardById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (!card.getUser().getId().equals(userDetails.getUserId())) {
            throw new SecurityException("Access denied!");
        }
        BigDecimal balance = cardService.getCardBalance(cardId);

        return ResponseEntity.ok(balance);
    }


    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @RequestBody TransferRequestDTO transferRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        transferService.transferBetweenCards(
                transferRequest.getFromCardId(),
                transferRequest.getToCardId(),
                userDetails.getUserId(),
                transferRequest.getAmount()
        );

        return ResponseEntity.ok().build();
    }
}