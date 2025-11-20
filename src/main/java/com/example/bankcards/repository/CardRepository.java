package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card,Long> {
    Page<Card> findByUserId(Long userId, Pageable pageable);
    boolean existsByUserIdAndCardStatus(Long userId, CardStatus cardStatus);
    boolean existsByCardNumber(String cardNumber);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT c FROM Card c where c.id=:id")
    Optional<Card> findByIdWithLock(@Param("id") Long id);
}
