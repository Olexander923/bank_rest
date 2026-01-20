package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.constants.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card,Long> {
    Page<Card> findByUserId(Long userId, Pageable pageable);
    boolean existsByUserIdAndCardStatus(Long userId, CardStatus cardStatus);
    boolean existsByCardNumber(String cardNumber);
    Page<Card> findByCardStatus(CardStatus cardStatus,Pageable pageable);
    Page<Card> findByExpireDateBefore(LocalDate date,Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findByIdForUpdate(@Param("id") Long id);
}
