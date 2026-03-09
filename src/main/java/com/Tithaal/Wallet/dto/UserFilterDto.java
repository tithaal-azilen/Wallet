package com.Tithaal.Wallet.dto;

import com.Tithaal.Wallet.entity.Role;
import com.Tithaal.Wallet.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterDto {
    private String username;
    private String email;
    private Role role;
    private UserStatus status;
    private Long organizationId;
    private String phoneNumber;
}
