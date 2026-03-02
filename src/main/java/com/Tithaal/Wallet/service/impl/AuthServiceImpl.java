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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);

        return jwtAuthResponse;
    }

    @Override
    public String createRefreshToken(String usernameOrEmail) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "User not found"));
        return refreshTokenService.createRefreshToken(user.getId());
    }

    @Override
    public String[] refreshToken(String requestRefreshToken) {
        return refreshTokenService.verifyAndRotate(requestRefreshToken);
    }

    @Override
    public void logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "User not found"));
            refreshTokenService.deleteByUserId(user.getId());
        }
    }
}
