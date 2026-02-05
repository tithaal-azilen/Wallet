package com.Tithaal.Wallet.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryDto {
    private Long id;
    private String username;
    private String city;
    private String phoneNumber;
}
