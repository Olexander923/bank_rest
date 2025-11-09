package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * для пользователя(мои карты,переводы, баланс)
 * GET методы для пользователей (мои карты)
 */
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class UserCardController {
    private final CardService cardService;

    @GetMapping
    //для получения всех карт пользователя
    public ResponseEntity<Page<CardResponseDTO>> getUserCards(
            @AuthenticationPrincipal CustomUserDetails userDetails, Pageable pageable){
        Long userId = userDetails.getUserId();
        Page<Card> cards = cardService.getUserCards(userId,pageable);
        Page<CardResponseDTO> cardDTOs = cards.map(card -> CardResponseDTO.convertToCardResponseDTO(card));
       return ResponseEntity.ok(cardDTOs);
    }



}