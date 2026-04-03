package com.Tithaal.Wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Wallet entity.
 *
 * user_id and tenant_id are stored as plain UUID columns (not entity FKs).
 * They are sourced from the Auth Service JWT claims — no local users table needed.
 */
@Entity
@Table(name = "wallet", indexes = {
        @Index(name = "idx_wallet_next_deduction_date", columnList = "next_deduction_date"),
        @Index(name = "idx_wallet_user_id", columnList = "user_id"),
        @Index(name = "idx_wallet_tenant_id", columnList = "tenant_id")
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

    /**
     * UUID of the owner — sourced from Auth Service (JWT "userId" claim).
     * No FK to a local users table.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * UUID of the tenant — sourced from Auth Service (JWT "tenantId" claim).
     * Used to scope queries to a single tenant.
     */
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "next_deduction_date")
    private java.time.LocalDate nextDeductionDate;

    @Column(name = "last_deduction_attempt")
    private java.time.LocalDate lastDeductionAttempt;

    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be greater than zero");
        }
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be greater than zero");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }
}