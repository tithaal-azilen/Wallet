package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.entity.RefreshToken;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.exception.TokenRefreshException;
import com.Tithaal.Wallet.repository.RefreshTokenRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.security.JwtTokenProvider;
import com.Tithaal.Wallet.service.RefreshTokenService;
import com.Tithaal.Wallet.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Value("${app.jwt-refresh-expiration-milliseconds}")
    private Long refreshTokenDurationMs;

    @Override
    @Transactional
    public String createRefreshToken(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found with id: " + userId));

        // Delete any existing refresh token for this user to enforce single-session
        // Or we could allow multiple, but let's delete existing for simplicity and
        // security.
        refreshTokenRepository.deleteByUser(user);

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = HashUtil.hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Override
    @Transactional
    public String[] verifyAndRotate(String rawToken) {
        String tokenHash = HashUtil.hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new TokenRefreshException("Refresh token is not in database!"));

        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token was expired. Please make a new signin request");
        }

        User user = refreshToken.getUser();

        // Rotate token
        String newRawToken = UUID.randomUUID().toString();
        String newTokenHash = HashUtil.hashToken(newRawToken);

        // Update the existing token with new hash and extended expiry
        refreshToken.setTokenHash(newTokenHash);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshTokenRepository.save(refreshToken);

        // Generate new Access Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        String newAccessToken = jwtTokenProvider.generateToken(authentication);

        return new String[] { newAccessToken, newRawToken };
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found with id: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }
}
