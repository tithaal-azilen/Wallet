package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.security.SecurityUtils;
import com.Tithaal.Wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User-scoped actions: creating a wallet for the authenticated user.
 *
 * Profile management (get profile, update profile, delete account, password change)
 * are owned by the Auth Service and have been removed.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Actions", description = "User-scoped wallet operations")
public class UserController {

    private final WalletService walletService;

    @Operation(summary = "Create Wallet", description = "Create a new wallet for the authenticated user")
    @PostMapping("/wallet")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> addWallet() {
        UUID userId   = SecurityUtils.getCurrentUserId();
        String tenantId = SecurityUtils.getCurrentTenantId();
        String result = walletService.addWallet(userId,
                tenantId != null ? UUID.fromString(tenantId) : null);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
