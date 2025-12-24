package com.example.bankcards.util;

import com.example.bankcards.dto.TransactionResponseDTO;
import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransactionMapper {
    private final CardEncryptionUtil cardEncryptionUtil;

    public TransactionResponseDTO transactionToDTO(Transaction transaction) {
        Card fromCard = transaction.getFromCard();
        Card toCard = transaction.getToCard();

        String fromCardNumber = cardEncryptionUtil.decrypt(fromCard.getCardNumber());
        String toCardNumber = cardEncryptionUtil.decrypt(toCard.getCardNumber());
        String maskedFromCard = "**** **** **** " + fromCardNumber.substring(fromCardNumber.length() - 4);
        String maskedToCard = "**** **** **** " + toCardNumber.substring(toCardNumber.length() - 4);

        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(transaction.getId());
        dto.setTransactionStatus(transaction.getTransactionStatus());
        dto.setAmount(transaction.getAmount());
        dto.setTimeStamp(transaction.getTimeStamp());
        dto.setFromMaskedCard(maskedFromCard);
        dto.setToMaskedCard(maskedToCard);
        return dto;
    }
}
