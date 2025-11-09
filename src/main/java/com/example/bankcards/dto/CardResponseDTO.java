package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * для передачи данных карты клиенту
 */
@Getter
@Setter
public class CardResponseDTO {
    private Long id;
    private Long userId;
    private String maskedNumber;
    private LocalDate expireDate;
    private CardStatus cardStatus;
    private BigDecimal balance;

   public static CardResponseDTO  convertToCardResponseDTO(Card card){
       CardResponseDTO dto = new CardResponseDTO();
       dto.setId(card.getId());
       dto.setMaskedNumber(card.getMaskedNumber());
       dto.setExpireDate(card.getExpireDate());
       dto.setCardStatus(card.getCardStatus());
       dto.setBalance(card.getBalance());
       dto.setUserId(card.getUser().getId());
       return dto;

   }
}
