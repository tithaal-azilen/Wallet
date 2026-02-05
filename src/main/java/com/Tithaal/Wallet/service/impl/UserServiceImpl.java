package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
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
    private final WalletTransactionRepository walletTransactionRepository;
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
                .city(registerDto.getCity())
                .phoneNumber(registerDto.getPhoneNumber())
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

    @Override
    public java.util.List<com.Tithaal.Wallet.dto.UserSummaryDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> com.Tithaal.Wallet.dto.UserSummaryDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .city(user.getCity())
                        .phoneNumber(user.getPhoneNumber())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public com.Tithaal.Wallet.dto.UserDetailDto getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user id: " + userId));

        return com.Tithaal.Wallet.dto.UserDetailDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .city(user.getCity())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .walletBalance(wallet.getBalance())
                .build();
    }

    @Override
    public User updateUser(Long userId, com.Tithaal.Wallet.dto.UpdateUserDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (updateDto.getCity() != null) {
            user.setCity(updateDto.getCity());
        }
        if (updateDto.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDto.getPhoneNumber());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        walletRepository.findByUser(user).ifPresent(wallet -> {
            walletTransactionRepository.deleteAllByWallet(wallet);
            walletRepository.delete(wallet);
        });
        userRepository.delete(user);
    }
}
