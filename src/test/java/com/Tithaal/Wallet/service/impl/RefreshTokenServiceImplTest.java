package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.entity.RefreshToken;
import com.Tithaal.Wallet.entity.Role;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.exception.TokenRefreshException;
import com.Tithaal.Wallet.redis.RefreshTokenRepository;
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
import java.util.UUID;

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

    // Dummy data for tests
    private User testUser;
    private RefreshToken testRefreshToken;
    private org.springframework.security.core.userdetails.User testUserDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 604800000L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPasswordHash("password");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.ROLE_USER);

        testRefreshToken = new RefreshToken();
        testRefreshToken.setTokenHash(HashUtil.hashToken(UUID.randomUUID().toString()));
        testRefreshToken.setUserId(testUser.getId());
        testRefreshToken.setExpiryDate(Instant.now().plusSeconds(3600));

        testUserDetails = new org.springframework.security.core.userdetails.User(
                testUser.getUsername(), testUser.getPasswordHash(), true, true, true, true,
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + testUser.getRole().name())));
    }

    @Test
    void createRefreshToken_ShouldReturnRawToken() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        String rawToken = refreshTokenService.createRefreshToken(userId);

        assertNotNull(rawToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyAndRotate_ShouldFailWhenTokenNotFound() {
        String rawToken = UUID.randomUUID().toString();
        String hash = HashUtil.hashToken(rawToken);

        when(refreshTokenRepository.findById(hash)).thenReturn(Optional.empty());

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.verifyAndRotate(rawToken));
    }

    @Test
    void verifyAndRotate_ShouldFailWhenExpired() {
        String rawToken = "expired-token";
        String hash = HashUtil.hashToken(rawToken);
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusSeconds(60));

        when(refreshTokenRepository.findById(hash)).thenReturn(Optional.of(token));

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.verifyAndRotate(rawToken));
        verify(refreshTokenRepository).delete(token);
    }
}
