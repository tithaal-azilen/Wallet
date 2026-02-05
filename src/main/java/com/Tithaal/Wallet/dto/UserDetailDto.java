package com.Tithaal.Wallet.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class UserDetailDto {
    private Long id;
    private String username;
    private String email;
    private String city;
    private String phoneNumber;
    private Instant createdAt;
    private BigDecimal walletBalance;
}
