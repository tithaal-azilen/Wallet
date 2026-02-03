package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterDto registerDto) {
        User user = userService.registerUser(registerDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@Valid @RequestBody LoginDto loginDto) {
        User user = userService.loginUser(loginDto);
        return ResponseEntity.ok(user);
    }
}
