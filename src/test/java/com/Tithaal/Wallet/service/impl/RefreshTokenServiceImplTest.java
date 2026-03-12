package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.entity.RefreshToken;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.exception.TokenRefreshException;
import com.Tithaal.Wallet.repository.RefreshTokenRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.security.JwtTokenProvider;
import com.Tithaal.Wallet.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 604800000L);
    }

    @Test
    void createRefreshToken_ShouldReturnRawToken() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        String rawToken = refreshTokenService.createRefreshToken(userId);

        assertNotNull(rawToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyAndRotate_ShouldFailWhenTokenNotFound() {
        String rawToken = "missing-token";
        String hash = HashUtil.hashToken(rawToken);

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.empty());

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.verifyAndRotate(rawToken));
    }

    @Test
    void verifyAndRotate_ShouldFailWhenExpired() {
        String rawToken = "expired-token";
        String hash = HashUtil.hashToken(rawToken);
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusSeconds(60));

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(token));

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.verifyAndRotate(rawToken));
        verify(refreshTokenRepository).delete(token);
    }
}
