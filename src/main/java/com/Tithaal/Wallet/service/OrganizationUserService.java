package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.UserSummaryDto;
import com.Tithaal.Wallet.entity.UserStatus;

public interface OrganizationUserService {
    PagedResponse<UserSummaryDto> getOrganizationUsers(Long orgId, Long adminId, int page, int size, String sortBy, String sortDir);
    
    void updateUserStatus(Long orgId, Long adminId, Long userId, UserStatus status);
}
