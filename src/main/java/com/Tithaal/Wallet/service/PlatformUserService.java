package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.UserSummaryDto;
import com.Tithaal.Wallet.entity.UserStatus;

import com.Tithaal.Wallet.dto.UserFilterDto;

public interface PlatformUserService {

    PagedResponse<UserSummaryDto> getAllUsers(UserFilterDto filter, int page, int size, String sortBy, String sortDir);

    void updateUserStatus(Long userId, UserStatus status);
}
