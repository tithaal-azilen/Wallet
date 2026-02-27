package com.Tithaal.Wallet.service;

public interface RefreshTokenService {
    String createRefreshToken(Long userId);

    String[] verifyAndRotate(String rawToken);

    void deleteByUserId(Long userId);
}
