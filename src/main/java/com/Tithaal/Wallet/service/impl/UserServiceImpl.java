package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.event.WalletCreatedEvent;
import com.Tithaal.Wallet.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * UserService — stripped to a minimal stub.
 *
 * Profile management (register, update, delete, get profile) is now owned by the Auth Service.
 * Wallet creation is delegated to WalletService.addWallet().
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    // All profile management removed — Auth Service owns it.
    // WalletService.addWallet(UUID, UUID) is called directly from UserController.
}
