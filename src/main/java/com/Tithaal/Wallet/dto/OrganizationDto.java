package com.Tithaal.Wallet.dto;

import com.Tithaal.Wallet.entity.OrganizationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class OrganizationDto {
    private Long id;
    private String name;
    private String orgCode;
    private OrganizationStatus status;
    private Instant createdAt;
}
