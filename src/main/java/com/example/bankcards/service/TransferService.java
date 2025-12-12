package com.example.bankcards.service;

import com.example.bankcards.entity.Card;;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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
    private final static BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("1000000.00");

    public TransferService(CardRepository cardRepository, TransactionRepository transactionRepository) {
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    //!важно на будещее! для postgres изоляция должна быть именно READ_COMMITTED, иначе будет ошибка сериализации,
    //REPEATABLE READ может откатывать транзакции при конфликтах записи, это особенность именно postgres
    public void transferBetweenCards(Long fromCardId, Long toCardId, Long userId, BigDecimal transferAmount) {
        if (transferAmount == null || transferAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Transfer amount must be positive".formatted(transferAmount));

        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }

        //блокировка карт чтобы избежать deadlock
        Long firstId = Math.min(fromCardId,toCardId);
        Long secondId = Math.max(fromCardId,toCardId);

        //блокировка для обновления
        Card firstCard = cardRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + firstId));

        Card secondCard = cardRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + secondId));

        Card fromCard = firstId.equals(fromCardId) ? firstCard : secondCard;
        Card toCard = firstId.equals(fromCardId) ? secondCard : firstCard;

        validateTransfer(fromCard,toCard,userId,transferAmount);

        //теперь делаем перевод
        fromCard.setBalance(fromCard.getBalance().subtract(transferAmount));
        toCard.setBalance(toCard.getBalance().add(transferAmount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transaction transaction = new Transaction(fromCard, toCard, transferAmount, SUCCESS);
        transactionRepository.save(transaction);

    }

    private void validateTransfer(Card fromCard, Card toCard, Long userId, BigDecimal amount){
        if (!fromCard.getUser().getId().equals(userId) || !toCard.getUser().getId().equals(userId)) {
            throw new SecurityException("Cards must belong to the same user.");
        }

        if (fromCard.getCardStatus() != CardStatus.ACTIVE || toCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Both cards must be active.");
        }

        if (fromCard.getExpireDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Sender card has expired.");
        }

        if (toCard.getExpireDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Recipient card has expired.");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds.");
        }

        if (amount.compareTo(MAX_TRANSFER_AMOUNT) > 0) {
            throw new IllegalStateException("Transfer amount exceeds maximum limit.");
        }
    }

}
