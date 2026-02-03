package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        User user = User.builder()
                .username(registerDto.getUsername())
                .email(registerDto.getEmail())
                .passwordHash(passwordEncoder.encode(registerDto.getPassword()))
                .createdAt(Instant.now())
                .build();

        User savedUser = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .build();

        walletRepository.save(wallet);

        return savedUser;
    }

    @Override
    public User loginUser(LoginDto loginDto) {
        User user = userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with username or email: " + loginDto.getUsernameOrEmail()));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password!");
        }

        return user;
    }
}
