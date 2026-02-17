package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.UpdateUserDto;
import com.Tithaal.Wallet.dto.UserDetailDto;
import com.Tithaal.Wallet.dto.UserSummaryDto;
import com.Tithaal.Wallet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations related to User Management")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get All Users", description = "Retrieve a list of all users")
    @GetMapping
    public ResponseEntity<List<UserSummaryDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Get User Details", description = "Retrieve details of a specific user by ID")
    @GetMapping("/{id}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<UserDetailDto> getUserDetails(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDetails(id));
    }

    @Operation(summary = "Update User", description = "Update details of an existing user")
    @PutMapping("/{id}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UpdateUserDto updateDto) {
        userService.updateUser(id, updateDto);
        return new ResponseEntity<>("User Updated Successfully!", HttpStatus.OK);
    }

    @Operation(summary = "Delete User", description = "Delete a user from the system")
    @DeleteMapping("/{id}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>("User Deleted Successfully!", HttpStatus.OK);
    }

    @Operation(summary = "Add Wallet to User", description = "Create a new wallet for a specific user")
    @PostMapping("/{id}/addwallet")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<String> addWallet(@PathVariable Long id) {
        String result = userService.addWallet(id);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
