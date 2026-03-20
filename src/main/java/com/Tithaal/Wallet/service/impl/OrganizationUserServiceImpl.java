package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.UserSummaryDto;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.UserStatus;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.service.OrganizationUserService;
import com.Tithaal.Wallet.service.validator.OrganizationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationUserServiceImpl implements OrganizationUserService {

    private final UserRepository userRepository;
    private final OrganizationValidator validator;

    @Override
    public PagedResponse<UserSummaryDto> getOrganizationUsers(Long orgId, Long adminId, int page, int size, String sortBy, String sortDir) {
        validator.validateAdminOwnership(orgId, adminId);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userRepository.findByOrganizationId(orgId, pageable);

        List<UserSummaryDto> content = users.getContent().stream()
                .map(u -> UserSummaryDto.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .city(u.getCity())
                        .phoneNumber(u.getPhoneNumber())
                        .build())
                .collect(Collectors.toList());

        return PagedResponse.<UserSummaryDto>builder()
                .content(content)
                .pageNo(users.getNumber())
                .pageSize(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .last(users.isLast())
                .build();
    }

    @Override
    @Transactional
    public void updateUserStatus(Long orgId, Long adminId, Long userId, UserStatus status) {
        validator.validateAdminOwnership(orgId, adminId);
        validator.validateActiveOrganization(orgId);

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "User not found with id: " + userId));

        if (targetUser.getOrganization() == null || !targetUser.getOrganization().getId().equals(orgId)) {
            throw new DomainException(ErrorType.FORBIDDEN, "User does not belong to your organization");
        }

        if (adminId.equals(userId) && (status == UserStatus.INACTIVE || status == UserStatus.SUSPENDED || status == UserStatus.DELETED)) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Admin cannot deactivate their own account");
        }

        if (targetUser.getStatus() == status) {
            return;
        }

        if (targetUser.getStatus() == UserStatus.DELETED && status == UserStatus.ACTIVE) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Cannot activate a deleted user");
        }

        targetUser.setStatus(status);
        userRepository.save(targetUser);
        log.info("Org Admin {} updated user {} status to {}", adminId, userId, status.name());
    }
}
