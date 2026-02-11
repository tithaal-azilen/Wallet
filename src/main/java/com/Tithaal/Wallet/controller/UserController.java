package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.UpdateUserDto;
import com.Tithaal.Wallet.dto.UserDetailDto;
import com.Tithaal.Wallet.dto.UserSummaryDto;
import com.Tithaal.Wallet.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserSummaryDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailDto> getUserDetails(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDetails(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UpdateUserDto updateDto) {
        userService.updateUser(id, updateDto);
        return new ResponseEntity<>("User Updated Successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>("User Deleted Successfully!", HttpStatus.OK);
    }

    @PostMapping("/{id}/addwallet")
    public ResponseEntity<String> addWallet(@PathVariable Long id) {
        String result = userService.addWallet(id);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
