package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.UserSummaryDto;
import com.Tithaal.Wallet.dto.UserFilterDto;
import com.Tithaal.Wallet.security.CustomUserDetails;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.UserStatus;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.service.PlatformUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformUserServiceImpl implements PlatformUserService {

    private final UserRepository userRepository;

    @Override
    public PagedResponse<UserSummaryDto> getAllUsers(UserFilterDto filter, int page, int size, String sortBy,
            String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> usersPage = userRepository.findAllWithFilters(
                filter.getUsername() != null ? filter.getUsername().trim() : null,
                filter.getEmail() != null ? filter.getEmail().trim() : null,
                filter.getRole(), filter.getStatus(),
                filter.getOrganizationId(), pageable);

        List<UserSummaryDto> content = usersPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast());
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "User not found with id: " + userId));

        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) auth.getPrincipal();
            if (currentUser.getId().equals(userId) && (status == UserStatus.INACTIVE || status == UserStatus.SUSPENDED
                    || status == UserStatus.DELETED)) {
                throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                        "Super Admin cannot deactivate their own account");
            }
        }

        if (user.getStatus() == status) {
            return;
        }

        if (user.getStatus() == UserStatus.DELETED && status == UserStatus.ACTIVE) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Cannot activate a deleted user");
        }

        user.setStatus(status);
        userRepository.save(user);
    }

    private UserSummaryDto mapToDto(User user) {
        UserSummaryDto dto = new UserSummaryDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setStatus(user.getStatus().name());
        dto.setOrganizationName(user.getOrganization() != null ? user.getOrganization().getName() : "None");
        return dto;
    }
}
