package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.UserSummaryDto;
import com.Tithaal.Wallet.dto.UserFilterDto;
import com.Tithaal.Wallet.entity.UserStatus;
import com.Tithaal.Wallet.service.PlatformUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform/users")
@RequiredArgsConstructor
@Tag(name = "Platform User Management", description = "Endpoints for Super Admin to manage users globally")
public class PlatformUserController {

    private final PlatformUserService platformUserService;

    @Operation(summary = "Get all users (Super Admin only)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<PagedResponse<UserSummaryDto>> getAllUsers(
            @ModelAttribute UserFilterDto filter,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "id", required = false) String sortBy,
            @RequestParam(defaultValue = "ASC", required = false) String sortDir) {

        PagedResponse<UserSummaryDto> users = platformUserService.getAllUsers(filter, page, size, sortBy, sortDir);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Update user status (Super Admin only)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{userId}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status) {

        platformUserService.updateUserStatus(userId, status);
        return ResponseEntity.ok("User status updated successfully to " + status);
    }
}
