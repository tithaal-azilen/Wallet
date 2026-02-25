package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.OrganizationRegistrationDto;
import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.exception.APIException;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
    void registerOrganizationAndAdmin_Success() {
        OrganizationRegistrationDto dto = new OrganizationRegistrationDto();
        dto.setOrgName("New Org");
        dto.setUsername("newAdmin");
        dto.setEmail("new@admin.com");
        dto.setPassword("password");

        when(organizationRepository.existsByName(dto.getOrgName())).thenReturn(false);
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrg);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

        String orgCode = organizationService.registerOrganizationAndAdmin(dto);

        assertNotNull(orgCode);
        assertTrue(orgCode.startsWith("ORG-"));
        verify(organizationRepository).save(any(Organization.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getOrganizationTransactions_Success() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(1L);
        transaction.setAmount(BigDecimal.TEN);
        transaction.setType(TransactionType.CREDIT);
        transaction.setBalanceAfter(BigDecimal.TEN);
        transaction.setCreatedAt(Instant.now());

        Wallet wallet = new Wallet();
        wallet.setId(1L);
        transaction.setWallet(wallet);

        Page<WalletTransaction> page = new PageImpl<>(List.of(transaction));

        when(userRepository.findByUsername(testAdmin.getUsername())).thenReturn(Optional.of(testAdmin));
        when(walletTransactionRepository.findByOrganizationId(eq(testOrg.getId()), any(Pageable.class)))
                .thenReturn(page);

        Page<WalletTransactionEntryDto> result = organizationService.getOrganizationTransactions(testOrg.getId(),
                testAdmin.getUsername(), Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals(BigDecimal.TEN, result.getContent().get(0).getAmount());
    }

    @Test
    void getOrganizationTransactions_ForbiddenForWrongOrg() {
        Organization wrongOrg = Organization.builder().id(2L).build();
        User wrongAdmin = User.builder().username("wrongAdmin").organization(wrongOrg).build();

        when(userRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(Optional.of(wrongAdmin));

        assertThrows(APIException.class, () -> organizationService.getOrganizationTransactions(testOrg.getId(),
                wrongAdmin.getUsername(), Pageable.unpaged()));
    }

    @Test
    void deleteOrganization_Success() {
        when(organizationRepository.findById(testOrg.getId())).thenReturn(Optional.of(testOrg));

        organizationService.deleteOrganization(testOrg.getId());

        assertEquals(OrganizationStatus.DELETED, testOrg.getStatus());
        verify(organizationRepository).save(testOrg);
    }
}
