package com.example.bankcards.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
@Entity
@Table(name = "card")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 19,nullable = false)
    private String cardNumber;
    private LocalDate expireDate;

    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus;

    @Column(precision = 19,scale = 2,nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    public Card(String cardNumber, LocalDate expireDate, CardStatus cardStatus, BigDecimal balance, User user) {
        this.cardNumber = cardNumber;
        this.expireDate = expireDate;
        this.cardStatus = cardStatus;
        this.balance = balance;
        this.user = user;
    }

    //todo вынести в отдельный класс?
    /**
     * маскировка карты
     */
    @Transient
    public String getMaskedNumber(){
     return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
