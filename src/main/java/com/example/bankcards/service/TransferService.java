package com.example.bankcards.service;

import com.example.bankcards.entity.Card;;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;;import static com.example.bankcards.entity.TransactionStatus.*;

/**
 * переводы между картами,тут же внутри вся валидация создание и обновление транзации
 */
@Service
public class TransferService {
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(CardRepository cardRepository, TransactionRepository transactionRepository) {
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
    }


    @Transactional
    public void transferBetweenCards(Long fromCardId, Long toCardId, Long userId, BigDecimal transferAmount) {
        if (transferAmount.signum() < 0)
            throw new IllegalArgumentException("Transfer amount must be positive".formatted(transferAmount));

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new IllegalArgumentException("Sender card not found"));
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient card not found"));

        if (fromCard.getCardStatus() != CardStatus.ACTIVE || toCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Both cards must be active.");
        }

        if (fromCard.getExpireDate().isBefore(LocalDate.now())
                || toCard.getExpireDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Card has expired.");
        }

        if (!fromCard.getUser().getId().equals(userId) || !toCard.getUser().getId().equals(userId)) {
            throw new SecurityException("Cards must belong to the same user.");
        }

        if (fromCard.getBalance().compareTo(transferAmount) < 0) {
            throw new IllegalStateException("Insufficient funds.");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(transferAmount));
        toCard.setBalance(toCard.getBalance().add(transferAmount));

        Transaction transaction = new Transaction(fromCard, toCard, transferAmount, SUCCESS);

        transactionRepository.save(transaction);
    }

}
