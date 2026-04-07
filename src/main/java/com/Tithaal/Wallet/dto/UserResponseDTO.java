package com.Tithaal.Wallet.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponseDTO {
    private UUID id;
    private String email;
    private String name;
    private String mobileNumber;
    private String roleName;
    private String tenantName;
    private LocalDate dob;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
