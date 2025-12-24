package com.example.bankcards.service;

import com.example.bankcards.dto.TransactionFilterDTO;
import com.example.bankcards.dto.TransactionResponseDTO;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.TransactionMapper;
import com.example.bankcards.util.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * сервис для работы с историей транзакций (поиск, фильтрация, экспорт)
 */
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public Page<TransactionResponseDTO> getFilteredTransaction(
            Long userId,
            TransactionFilterDTO filter,
            Pageable pageable)
    {
        Objects.requireNonNull(userId,"User ID cannot be null.");

        TransactionStatus status = filter.getStatus();
        BigDecimal minAmount = filter.getMinAmount();
        BigDecimal maxAmount = filter.getMaxAmount();
        //создание спецификации для каждого фильтра
        Specification<Transaction> spec1 = TransactionSpecification.withStatus(status);
        Specification<Transaction> spec2 = TransactionSpecification.withMinAmount(minAmount);
        Specification<Transaction> spec3 = TransactionSpecification.withMaxAmount(maxAmount);
        Specification<Transaction> spec4 = TransactionSpecification.withUserId(userId);
        Specification<Transaction> spec5 = TransactionSpecification.withTimeStampBetween(
                filter.getStartDate(),filter.getEndDate());
        List<Specification<Transaction>> specs = Arrays.asList(spec1,spec2,spec3,spec4,spec5);
        //проерка на null, соединение всех спеков
        Specification<Transaction> combined = specs.stream()
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(null);

        Page<Transaction> transactions = transactionRepository.findAll(combined,pageable);
        Page<TransactionResponseDTO> dtoPage = transactions.map(transactionMapper::transactionToDTO);

        return dtoPage;
    }


    //TransactionService.exportTransactions() — экспортирует выписки ???? под вопросом, надо ли переделать из контроллера
}
