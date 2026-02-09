package com.Tithaal.Wallet.dto;

import lombok.Data;

@Data
public class UpdateUserDto {
    private String city;
    private String phoneNumber;
    private String password;
    private String email;
    private String username;
}
