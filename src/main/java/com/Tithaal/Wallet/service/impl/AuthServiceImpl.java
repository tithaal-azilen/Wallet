package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.JwtAuthResponse;
import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.security.JwtTokenProvider;
import com.Tithaal.Wallet.service.AuthService;
import com.Tithaal.Wallet.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Override
    public JwtAuthResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail())
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "User not found"));
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);
        jwtAuthResponse.setRefreshToken(refreshToken);

        log.info("User {} logged in successfully", loginDto.getUsernameOrEmail());

        return jwtAuthResponse;
    }

    @Override
    public String createRefreshToken(String usernameOrEmail) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "User not found"));
        return refreshTokenService.createRefreshToken(user.getId());
    }

    @Override
    public JwtAuthResponse refreshToken(String requestRefreshToken) {
        String[] tokens = refreshTokenService.verifyAndRotate(requestRefreshToken);
        JwtAuthResponse response = new JwtAuthResponse();
        response.setAccessToken(tokens[0]);
        response.setRefreshToken(tokens[1]);
        return response;
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.deleteByToken(refreshToken);
            log.info("User logged out using refresh token.");
        }
    }
}
