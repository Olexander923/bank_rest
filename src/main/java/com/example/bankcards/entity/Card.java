package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardNumber;
    private LocalDate expireDate;

    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus;

    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public Card(String cardNumber, LocalDate expireDate, CardStatus cardStatus, BigDecimal balance, User user) {
        this.cardNumber = cardNumber;
        this.expireDate = expireDate;
        this.cardStatus = cardStatus;
        this.balance = balance;
        this.user = user;
    }

    /**
     * маскировка карты
     */
    @Transient
    public String getMaskedNumber(){
     return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
