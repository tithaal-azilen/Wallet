package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.Tithaal.Wallet.service.validator.OrganizationValidator validator;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private Organization testOrg;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testOrg = Organization.builder()
                .id(1L)
                .name("Test Org")
                .orgCode("ORG-1234X")
                .status(OrganizationStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        testAdmin = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@test.com")
                .role(Role.ROLE_ORG_ADMIN)
                .organization(testOrg)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void deleteOrganization_Success() {
        doNothing().when(validator).validateAdminOwnership(testOrg.getId(), testAdmin.getId());
        when(organizationRepository.findById(testOrg.getId())).thenReturn(Optional.of(testOrg));

        organizationService.deleteOrganization(testOrg.getId(), testAdmin.getId());

        assertEquals(OrganizationStatus.DELETED, testOrg.getStatus());
        verify(organizationRepository).save(testOrg);
    }
}
