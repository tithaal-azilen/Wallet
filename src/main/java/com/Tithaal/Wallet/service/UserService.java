package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.dto.UpdateUserDto;
import com.Tithaal.Wallet.dto.UserDetailDto;
import com.Tithaal.Wallet.dto.UserSummaryDto;
import com.Tithaal.Wallet.entity.User;

public interface UserService {
    User registerUser(RegisterDto registerDto);

    User loginUser(LoginDto loginDto);

    java.util.List<UserSummaryDto> getAllUsers();

    UserDetailDto getUserDetails(Long userId);

    User updateUser(Long userId, UpdateUserDto updateDto);

    void deleteUser(Long userId);
}
