package com.example.bankcards.entity;
import com.example.bankcards.constants.CardStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
@Entity
@Table(name = "card")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false,unique = true)
    private String cardNumber;

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_status")
    private CardStatus cardStatus;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Card(String cardNumber, LocalDate expireDate, CardStatus cardStatus, BigDecimal balance, User user) {
        this.cardNumber = cardNumber;
        this.expireDate = expireDate;
        this.cardStatus = cardStatus;
        this.balance = balance;
        this.user = user;
    }
}
