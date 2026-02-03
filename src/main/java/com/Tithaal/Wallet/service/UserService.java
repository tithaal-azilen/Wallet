package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.entity.User;

public interface UserService {
    User registerUser(RegisterDto registerDto);

    User loginUser(LoginDto loginDto);
}
