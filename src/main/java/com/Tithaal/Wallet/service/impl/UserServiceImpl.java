package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.dto.UpdateUserDto;
import com.Tithaal.Wallet.dto.UserDetailDto;
import com.Tithaal.Wallet.dto.UserSummaryDto;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.service.UserService;
import com.Tithaal.Wallet.exception.APIException;
import com.Tithaal.Wallet.event.WalletCreatedEvent;
import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.Role;
import com.Tithaal.Wallet.entity.UserStatus;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public String registerUser(RegisterDto registerDto) {
        if (registerDto.getUsername() == null || registerDto.getUsername().trim().isEmpty()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Username cannot be null or empty!");
        }
        if (registerDto.getEmail() == null || registerDto.getEmail().trim().isEmpty()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Email cannot be null or empty!");
        }
        if (registerDto.getPassword() == null || registerDto.getPassword().trim().isEmpty()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Password cannot be null or empty!");
        }
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Username is already taken!");
        }
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Email is already taken!");
        }

        User user = User.builder()
                .username(registerDto.getUsername())
                .email(registerDto.getEmail())
                .city(registerDto.getCity())
                .phoneNumber(registerDto.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(registerDto.getPassword()))
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        if (registerDto.getOrgCode() != null && !registerDto.getOrgCode().trim().isEmpty()) {
            Organization organization = organizationRepository.findByOrgCode(registerDto.getOrgCode())
                    .orElseThrow(() -> new APIException(HttpStatus.BAD_REQUEST, "Invalid organization code!"));
            user.setOrganization(organization);
        }

        if (userRepository.save(user) != null) {
            return "User Registered Successfully!";
        } else {
            return "User Registration Failed!";
        }
    }

    @Override
    public String addWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException(HttpStatus.BAD_REQUEST, "User not found with id: " + userId));

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .nextDeductionDate(calculateNextDeductionDate())
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        if (savedWallet != null) {
            eventPublisher.publishEvent(new WalletCreatedEvent(this, user.getEmail(), savedWallet.getId()));
            return "Wallet Added Successfully! with wallet id: " + savedWallet.getId();
        } else {
            return "Wallet Addition Failed!";
        }
    }

    @Override
    public String loginUser(LoginDto loginDto) {
        if (loginDto.getUsernameOrEmail() == null || loginDto.getUsernameOrEmail().trim().isEmpty()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Username or Email cannot be null or empty!");
        }
        if (loginDto.getPassword() == null || loginDto.getPassword().trim().isEmpty()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Password cannot be null or empty!");
        }

        User user = userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail())
                .orElseThrow(() -> new APIException(HttpStatus.BAD_REQUEST,
                        "User not found with username or email: " + loginDto.getUsernameOrEmail()));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new APIException(HttpStatus.FORBIDDEN, "Account is disabled. Please contact support.");
        }

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Invalid password!");
        }

        return "User Logged In Successfully!";
    }

    @Override
    public java.util.List<UserSummaryDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserSummaryDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .city(user.getCity())
                        .phoneNumber(user.getPhoneNumber())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public UserDetailDto getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException(HttpStatus.BAD_REQUEST,
                        "User not found with id: " + userId));

        java.util.List<Wallet> wallets = walletRepository.findByUser(user);

        return UserDetailDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .city(user.getCity())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .wallets(wallets.stream()
                        .map(wallet -> com.Tithaal.Wallet.dto.WalletDto.builder()
                                .id(wallet.getId())
                                .balance(wallet.getBalance())
                                .createdAt(wallet.getCreatedAt())
                                .build())
                        .collect(java.util.stream.Collectors.toList()))
                .build();
    }

    @Override
    public void updateUser(Long userId, UpdateUserDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException(HttpStatus.BAD_REQUEST,
                        "User not found with id: " + userId));

        if (updateDto.getCity() != null) {
            user.setCity(updateDto.getCity());
        }
        if (updateDto.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDto.getPhoneNumber());
        }
        if (updateDto.getPassword() != null && !updateDto.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(updateDto.getPassword()));
        }
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new APIException(HttpStatus.BAD_REQUEST, "Email is already taken!");
            }
            user.setEmail(updateDto.getEmail());
        }
        if (updateDto.getUsername() != null && !updateDto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateDto.getUsername())) {
                throw new APIException(HttpStatus.BAD_REQUEST, "Username is already taken!");
            }
            user.setUsername(updateDto.getUsername());
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException(HttpStatus.BAD_REQUEST,
                        "User not found with id: " + userId));

        java.util.List<Wallet> wallets = walletRepository.findByUser(user);
        for (Wallet wallet : wallets) {
            if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                throw new APIException(HttpStatus.BAD_REQUEST,
                        "Cannot delete user. Wallet with id " + wallet.getId() + " has active balance.");
            }
        }
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    private java.time.LocalDate calculateNextDeductionDate() {
        java.time.LocalDate today = java.time.LocalDate.now();
        int dayOfMonth = today.getDayOfMonth();
        java.time.LocalDate nextDeductionDate;

        if (dayOfMonth <= 7) {
            nextDeductionDate = today.withDayOfMonth(1).plusMonths(1);
        } else if (dayOfMonth <= 14) {
            nextDeductionDate = today.withDayOfMonth(8).plusMonths(1);
        } else if (dayOfMonth <= 21) {
            nextDeductionDate = today.withDayOfMonth(15).plusMonths(1);
        } else {
            nextDeductionDate = today.withDayOfMonth(22).plusMonths(1);
        }
        return nextDeductionDate;
    }
}
