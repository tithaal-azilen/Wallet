package com.Tithaal.Wallet.service;

import java.util.List;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.dto.UpdateUserDto;
import com.Tithaal.Wallet.dto.UserDetailDto;
import com.Tithaal.Wallet.dto.UserSummaryDto;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.Wallet;

public interface UserService {
    User registerUser(RegisterDto registerDto);

    Wallet addWallet(Long userId);

    User loginUser(LoginDto loginDto);

    List<UserSummaryDto> getAllUsers();

    UserDetailDto getUserDetails(Long userId);

    User updateUser(Long userId, UpdateUserDto updateDto);

    void deleteUser(Long userId);
}
