package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequestDTO;
import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * для ADMIN (все карты, управление)
 * POST, PUT, DELETE для админов (управление картами)
 */
@RestController
@RequiredArgsConstructor
public class AdminCardController {
    private final CardService cardService;

    @PostMapping("/api/admin/cards")
    public ResponseEntity<CardResponseDTO> createCards(
            @RequestBody CardCreateRequestDTO createRequestDTO
    ){
        Card card = cardService.createCard(createRequestDTO, createRequestDTO.getUserId());
        CardResponseDTO responseDTO = CardResponseDTO.convertToCardResponseDTO(card);
        return ResponseEntity.ok(responseDTO);
    }
}
