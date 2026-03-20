package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.JwtAuthResponse;
import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.service.UserService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import com.Tithaal.Wallet.service.AuthService;
import com.Tithaal.Wallet.dto.TokenRefreshRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Operations related to User Authentication")
public class AuthController {

        private final UserService userService;
        private final AuthService authService;

        @Operation(summary = "Register User", description = "Register a new user in the system")
        @PostMapping("/register")
        public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto) {
                String result = userService.registerUser(registerDto);
                return new ResponseEntity<>(result, HttpStatus.CREATED);
        }

        @Operation(summary = "Login User", description = "Login user and return JWT token in body, refresh token in cookie")
        @PostMapping("/login")
        public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginDto loginDto) {
                JwtAuthResponse jwtAuthResponse = authService.login(loginDto);

                ResponseCookie cookie = ResponseCookie.from("refreshToken", jwtAuthResponse.getRefreshToken())
                                .httpOnly(true)
                                .secure(false)
                                .path("/api/auth")
                                .maxAge(7 * 24 * 60 * 60)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body(jwtAuthResponse);
        }

        @Operation(summary = "Refresh Token", description = "Get a new access token using a refresh token from cookie or body")
        @PostMapping("/refreshtoken")
        public ResponseEntity<JwtAuthResponse> refreshtoken(
                        @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
                        @RequestBody(required = false) TokenRefreshRequest request) {
                
                String refreshTokenToUse = cookieRefreshToken;
                if (refreshTokenToUse == null && request != null) {
                        refreshTokenToUse = request.getRefreshToken();
                }

                if (refreshTokenToUse == null || refreshTokenToUse.isEmpty()) {
                        throw new IllegalArgumentException("Refresh Token is required!");
                }

                JwtAuthResponse jwtAuthResponse = authService.refreshToken(refreshTokenToUse);

                ResponseCookie cookie = ResponseCookie.from("refreshToken", jwtAuthResponse.getRefreshToken())
                                .httpOnly(true)
                                .secure(false)
                                .path("/api/auth")
                                .maxAge(7 * 24 * 60 * 60)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body(jwtAuthResponse);
        }

        @Operation(summary = "Logout User", description = "Invalidate refresh token and clear cookie")
        @PostMapping("/logout")
        public ResponseEntity<String> logoutUser(
                        @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
                        @RequestBody(required = false) TokenRefreshRequest request) {
                
                String refreshTokenToUse = cookieRefreshToken;
                if (refreshTokenToUse == null && request != null) {
                        refreshTokenToUse = request.getRefreshToken();
                }

                authService.logout(refreshTokenToUse);

                ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                                .httpOnly(true)
                                .secure(false)
                                .path("/api/auth")
                                .maxAge(0)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body("You've been logged out!");
        }
}
