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
    private java.util.UUID testAdminId;


    @BeforeEach
    void setUp() {
        testOrg = Organization.builder()
                .id(1L)
                .name("Test Org")
                .orgCode("ORG-1234X")
                .status(OrganizationStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        testAdminId = java.util.UUID.randomUUID();

    }

    @Test
    void deleteOrganization_Success() {
        doNothing().when(validator).validateAdminOwnership(testOrg.getId(), testAdminId);
        when(organizationRepository.findById(testOrg.getId())).thenReturn(Optional.of(testOrg));

        organizationService.deleteOrganization(testOrg.getId(), testAdminId);


        assertEquals(OrganizationStatus.DELETED, testOrg.getStatus());
        verify(organizationRepository).save(testOrg);
    }
}
