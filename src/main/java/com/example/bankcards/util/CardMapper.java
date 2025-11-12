package com.example.bankcards.util;

import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardMapper {
    private final CardEncryptionUtil cardEncryptionUtil;

    public CardResponseDTO toDTO(Card card) {
        String originalNumber = cardEncryptionUtil.decrypt(card.getCardNumber());
        String masked = "**** **** **** " + originalNumber.substring(originalNumber.length() - 4);

        CardResponseDTO dto = new CardResponseDTO();
        dto.setId(card.getId());
        dto.setUserId(card.getUser().getId());
        dto.setMaskedNumber(masked);
        dto.setExpireDate(card.getExpireDate());
        dto.setCardStatus(card.getCardStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }
}
