package com.Tithaal.Wallet.service;

import java.util.List;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.dto.UpdateUserDto;
import com.Tithaal.Wallet.dto.UserDetailDto;
import com.Tithaal.Wallet.dto.UserSummaryDto;

public interface UserService {
    String registerUser(RegisterDto registerDto);

    String loginUser(LoginDto loginDto);

    String addWallet(Long userId);

    List<UserSummaryDto> getAllUsers();

    UserDetailDto getUserDetails(Long userId);

    void updateUser(Long userId, UpdateUserDto updateDto);

    void deleteUser(Long userId);
}
