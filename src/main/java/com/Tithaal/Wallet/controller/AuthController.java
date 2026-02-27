package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.JwtAuthResponse;
import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.security.JwtTokenProvider;
import com.Tithaal.Wallet.service.UserService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.Tithaal.Wallet.service.RefreshTokenService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Operations related to User Authentication")
public class AuthController {

        private final UserService userService;
        private final AuthenticationManager authenticationManager;
        private final JwtTokenProvider jwtTokenProvider;
        private final RefreshTokenService refreshTokenService;
        private final UserRepository userRepository;

        @Operation(summary = "Register User", description = "Register a new user in the system")
        @PostMapping("/register")
        public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto) {
                String result = userService.registerUser(registerDto);
                return new ResponseEntity<>(result, HttpStatus.CREATED);
        }

        @Operation(summary = "Login User", description = "Login user and return JWT token in body, refresh token in cookie")
        @PostMapping("/login")
        public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginDto loginDto) {
                Authentication authentication = authenticationManager
                                .authenticate(new UsernamePasswordAuthenticationToken(
                                                loginDto.getUsernameOrEmail(), loginDto.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                String token = jwtTokenProvider.generateToken(authentication);

                User user = userRepository
                                .findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                String refreshToken = refreshTokenService.createRefreshToken(user.getId());

                ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                                .httpOnly(true)
                                .secure(false)
                                .path("/api/auth")
                                .maxAge(7 * 24 * 60 * 60)
                                .build();

                JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
                jwtAuthResponse.setAccessToken(token);

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body(jwtAuthResponse);
        }

        @Operation(summary = "Refresh Token", description = "Get a new access token using a refresh token cookie")
        @PostMapping("/refreshtoken")
        public ResponseEntity<JwtAuthResponse> refreshtoken(
                        @CookieValue(name = "refreshToken") String requestRefreshToken) {
                String[] tokens = refreshTokenService.verifyAndRotate(requestRefreshToken);

                ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens[1])
                                .httpOnly(true)
                                .secure(false)
                                .path("/api/auth")
                                .maxAge(7 * 24 * 60 * 60)
                                .build();

                JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
                jwtAuthResponse.setAccessToken(tokens[0]);

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body(jwtAuthResponse);
        }

        @Operation(summary = "Logout User", description = "Invalidate refresh token and clear cookie")
        @PostMapping("/logout")
        public ResponseEntity<String> logoutUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()
                                && !authentication.getPrincipal().equals("anonymousUser")) {
                        String username = authentication.getName();
                        User user = userRepository.findByUsername(username)
                                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                        refreshTokenService.deleteByUserId(user.getId());
                }

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
