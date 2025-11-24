package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequestDTO;
import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import com.example.bankcards.util.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * для ADMIN (управление картами)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {
    private final CardService cardService;
    private final CardMapper cardMapper;


    @PostMapping
    public ResponseEntity<CardResponseDTO> createCard(@Valid
            @RequestBody CardCreateRequestDTO createRequestDTO
    ) {
            Card card = cardService.createCard(createRequestDTO, createRequestDTO.getUserId());
            System.out.println("Service returned card: " + card);
            return ResponseEntity.ok(cardMapper.toDTO(card));
    }


    @DeleteMapping("{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{cardId}/block")
    public ResponseEntity<CardResponseDTO> blockCard(@PathVariable Long cardId) {
        Card card = cardService.blockCard(cardId);
        return ResponseEntity.ok(cardMapper.toDTO(card));
    }


    @PatchMapping("/{cardId}/activate")
    public ResponseEntity<CardResponseDTO> activateCard(@PathVariable Long cardId) {
        Card card = cardService.activateCard(cardId);
        return ResponseEntity.ok(cardMapper.toDTO(card));
    }


    @GetMapping
    public ResponseEntity<List<CardResponseDTO>> getAllCards() {
        List<Card> cards = cardService.getAllCards();
        List<CardResponseDTO> cardDTOs = cards.stream()
                .map(cardMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cardDTOs);
    }
}
