package com.example.bankcards.entity;

import com.example.bankcards.constants.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * сущность для передачи данных в dto при создании запроса на блокировку от user'а
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "block_card_request")
public class BlockRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Card card;

    @ManyToOne
    private User user;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus requestStatus;
}
