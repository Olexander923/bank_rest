package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequestDTO;
import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
            return ResponseEntity.ok(cardMapper.cardToDTO(card));
    }


    @DeleteMapping("{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{cardId}/block")
    public ResponseEntity<CardResponseDTO> blockCard(@PathVariable Long cardId) {
        Card card = cardService.blockCard(cardId);
        return ResponseEntity.ok(cardMapper.cardToDTO(card));
    }


    @PatchMapping("/{cardId}/activate")
    public ResponseEntity<CardResponseDTO> activateCard(@PathVariable Long cardId) {
        Card card = cardService.activateCard(cardId);
        return ResponseEntity.ok(cardMapper.cardToDTO(card));
    }

    @GetMapping("/expiring")
    public ResponseEntity<Page<CardResponseDTO>> getAllExpiringDateCards(//можно менять дату для фильтрации
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
            ) {
        Page<Card> cards = cardService.getCardsWithExpiringDateBefore(date, pageable);
        return ResponseEntity.ok(cards.map(cardMapper::cardToDTO));

    }

    @GetMapping
    public ResponseEntity<Page<CardResponseDTO>> getAllCards(
            Pageable pageable,
            @RequestParam(required = false) CardStatus status) {
        Page<Card> cards = (status != null)
            ? cardService.getAllCardsByStatus(status,pageable):cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards.map(cardMapper::cardToDTO));
        }
    }




