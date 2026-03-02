package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.JwtAuthResponse;
import com.Tithaal.Wallet.dto.LoginDto;
import org.springframework.security.core.Authentication;

public interface AuthService {
    JwtAuthResponse login(LoginDto loginDto);

    String createRefreshToken(String usernameOrEmail);

    String[] refreshToken(String requestRefreshToken);

    void logout(Authentication authentication);
}
