package com.Tithaal.Wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallet", indexes = {
        @Index(name = "idx_wallet_next_deduction_date", columnList = "next_deduction_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "next_deduction_date")
    private java.time.LocalDate nextDeductionDate;

    @Column(name = "last_deduction_attempt")
    private java.time.LocalDate lastDeductionAttempt;
}