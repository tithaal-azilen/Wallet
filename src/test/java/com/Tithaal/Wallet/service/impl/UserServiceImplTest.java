package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.*;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.UserStatus;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.exception.APIException;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    // --- registerUser Tests ---

    @Test
    void registerUser_Success() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password");
        registerDto.setCity("Test City");
        registerDto.setPhoneNumber("1234567890");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        String result = userService.registerUser(registerDto);

        assertEquals("User Registered Successfully!", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_Fail_UsernameTaken() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("existinguser");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        APIException exception = assertThrows(APIException.class, () -> userService.registerUser(registerDto));
        assertEquals("Username is already taken!", exception.getMessage());
    }

    @Test
    void registerUser_Fail_EmailTaken() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("existing@example.com");
        registerDto.setPassword("password");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        APIException exception = assertThrows(APIException.class, () -> userService.registerUser(registerDto));
        assertEquals("Email is already taken!", exception.getMessage());
    }

    @Test
    void registerUser_Fail_NullOrEmptyFields() {
        RegisterDto registerDto1 = new RegisterDto(); // Null fields
        assertThrows(APIException.class, () -> userService.registerUser(registerDto1));

        RegisterDto registerDto2 = new RegisterDto();
        registerDto2.setUsername("");
        assertThrows(APIException.class, () -> userService.registerUser(registerDto2));

        RegisterDto registerDto3 = new RegisterDto();
        registerDto3.setUsername("user");
        registerDto3.setEmail("");
        assertThrows(APIException.class, () -> userService.registerUser(registerDto3));

        RegisterDto registerDto4 = new RegisterDto();
        registerDto4.setUsername("user");
        registerDto4.setEmail("email@test.com");
        registerDto4.setPassword("");
        assertThrows(APIException.class, () -> userService.registerUser(registerDto4));
    }

    // --- addWallet Tests ---

    @Test
    void addWallet_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.save(any(Wallet.class))).thenReturn(new Wallet());

        String result = userService.addWallet(userId);

        assertEquals("Wallet Added Successfully! with wallet id: null", result);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void addWallet_Fail_UserNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () -> userService.addWallet(userId));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    // --- loginUser Tests ---

    @Test
    void loginUser_Success() {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("password");

        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("encodedPassword");

        when(userRepository.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        String result = userService.loginUser(loginDto);

        assertEquals("User Logged In Successfully!", result);
    }

    @Test
    void loginUser_Fail_UserNotFound() {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("unknown");
        loginDto.setPassword("password");

        when(userRepository.findByUsernameOrEmail("unknown", "unknown")).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () -> userService.loginUser(loginDto));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void loginUser_Fail_InvalidPassword() {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("wrongpassword");

        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("encodedPassword");

        when(userRepository.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        APIException exception = assertThrows(APIException.class, () -> userService.loginUser(loginDto));
        assertEquals("Invalid password!", exception.getMessage());
    }

    @Test
    void loginUser_Fail_NullOrEmpty() {
        LoginDto loginDto1 = new LoginDto();
        assertThrows(APIException.class, () -> userService.loginUser(loginDto1));

        LoginDto loginDto2 = new LoginDto();
        loginDto2.setUsernameOrEmail("user");
        loginDto2.setPassword("");
        assertThrows(APIException.class, () -> userService.loginUser(loginDto2));
    }

    // --- getAllUsers Tests ---

    @Test
    void getAllUsers_Success() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserSummaryDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).getUsername());
    }

    // --- getUserDetails Tests ---

    @Test
    void getUserDetails_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        Wallet wallet = new Wallet();
        wallet.setId(101L);
        wallet.setBalance(BigDecimal.TEN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Collections.singletonList(wallet));

        UserDetailDto userDetails = userService.getUserDetails(userId);

        assertEquals("testuser", userDetails.getUsername());
        assertEquals(1, userDetails.getWallets().size());
        assertEquals(BigDecimal.TEN, userDetails.getWallets().get(0).getBalance());
    }

    @Test
    void getUserDetails_Fail_UserNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(APIException.class, () -> userService.getUserDetails(userId));
    }

    // --- updateUser Tests ---

    @Test
    void updateUser_Success_FullUpdate() {
        Long userId = 1L;
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setCity("New City");
        updateDto.setPhoneNumber("9876543210");
        updateDto.setEmail("new@example.com");
        updateDto.setUsername("newuser");
        updateDto.setPassword("newpassword");

        User user = new User();
        user.setId(userId);
        user.setEmail("old@example.com");
        user.setUsername("olduser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");

        userService.updateUser(userId, updateDto);

        assertEquals("New City", user.getCity());
        assertEquals("9876543210", user.getPhoneNumber());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("newuser", user.getUsername());
        assertEquals("encodedNewPassword", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_Success_PartialUpdate() {
        Long userId = 1L;
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setCity("New City");
        // other fields null

        User user = new User();
        user.setId(userId);
        user.setCity("Old City");
        user.setPhoneNumber("123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUser(userId, updateDto);

        assertEquals("New City", user.getCity());
        assertEquals("123", user.getPhoneNumber()); // Should verify unchanged
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_Fail_EmailTaken() {
        Long userId = 1L;
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setEmail("taken@example.com");

        User user = new User();
        user.setId(userId);
        user.setEmail("current@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        APIException exception = assertThrows(APIException.class, () -> userService.updateUser(userId, updateDto));
        assertEquals("Email is already taken!", exception.getMessage());
    }

    // --- deleteUser Tests ---

    @Test
    void deleteUser_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Wallet wallet = new Wallet();
        wallet.setId(101L);
        wallet.setBalance(BigDecimal.ZERO);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Collections.singletonList(wallet));

        userService.deleteUser(userId);

        assertEquals(UserStatus.INACTIVE, user.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_Fail_ActiveBalance() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Wallet wallet = new Wallet();
        wallet.setId(101L);
        wallet.setBalance(BigDecimal.TEN); // Active balance

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Collections.singletonList(wallet));

        APIException exception = assertThrows(APIException.class, () -> userService.deleteUser(userId));
        assertTrue(exception.getMessage().contains("has active balance"));

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_Fail_UserNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(APIException.class, () -> userService.deleteUser(userId));
    }
}
