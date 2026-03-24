package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.entity.RefreshToken;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.exception.TokenRefreshException;
import com.Tithaal.Wallet.redis.RefreshTokenRepository;
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

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = HashUtil.hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    RefreshToken rt = new RefreshToken();
                    rt.setUserId(user.getId());
                    return rt;
                });

        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setExpiration(refreshTokenDurationMs / 1000); // TTL in seconds

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Override
    @Transactional
    public String[] verifyAndRotate(String rawToken) {
        String tokenHash = HashUtil.hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findById(tokenHash)
                .orElseThrow(() -> new TokenRefreshException("Refresh token is not in database!"));

        // Explicit expiry check is technically redundant with TTL but kept for safety in edge cases
        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token was expired. Please make a new signin request");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newRawToken = UUID.randomUUID().toString();
        String newTokenHash = HashUtil.hashToken(newRawToken);

        // Delete old token hash to prevent duplicate accumulation since @Id is tokenHash
        refreshTokenRepository.deleteById(tokenHash);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUserId(user.getId());
        newRefreshToken.setTokenHash(newTokenHash);
        newRefreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        newRefreshToken.setExpiration(refreshTokenDurationMs / 1000);
        refreshTokenRepository.save(newRefreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        String newAccessToken = jwtTokenProvider.generateToken(authentication);

        return new String[] { newAccessToken, newRawToken };
    }


    @Override
    @Transactional
    public void deleteByToken(String rawToken) {
        String tokenHash = HashUtil.hashToken(rawToken);
        refreshTokenRepository.deleteById(tokenHash);
    }
}
