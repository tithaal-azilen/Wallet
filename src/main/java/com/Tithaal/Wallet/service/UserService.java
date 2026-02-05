package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.entity.User;

public interface UserService {
    User registerUser(RegisterDto registerDto);

    User loginUser(LoginDto loginDto);

    java.util.List<com.Tithaal.Wallet.dto.UserSummaryDto> getAllUsers();

    com.Tithaal.Wallet.dto.UserDetailDto getUserDetails(Long userId);

    User updateUser(Long userId, com.Tithaal.Wallet.dto.UpdateUserDto updateDto);

    void deleteUser(Long userId);
}
