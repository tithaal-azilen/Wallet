package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.Role;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.UUID;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization org;
    private String uniqueSuffix;

    @BeforeEach
    void setUp() {
        uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        org = Organization.builder()
                .name("Test Org " + uniqueSuffix)
                .orgCode("TESTORG_" + uniqueSuffix)
                .createdAt(Instant.now())
                .status(com.Tithaal.Wallet.entity.OrganizationStatus.ACTIVE)
                .build();
        organizationRepository.save(org);

        User user1 = User.builder()
                .username("john_doe_" + uniqueSuffix)
                .email("john_" + uniqueSuffix + "@example.com")
                .passwordHash("hash")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .organization(org)
                .createdAt(Instant.now())
                .build();
        
        User user2 = User.builder()
                .username("jane_admin_" + uniqueSuffix)
                .email("jane_" + uniqueSuffix + "@example.com")
                .passwordHash("hash")
                .role(Role.ROLE_ORG_ADMIN)
                .status(UserStatus.ACTIVE)
                .organization(org)
                .createdAt(Instant.now())
                .build();

        userRepository.save(user1);
        userRepository.save(user2);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        organizationRepository.delete(org);
    }

    @Test
    void findAllWithFilters_ShouldFilterByPartialUsername() {
        Page<User> result = userRepository.findAllWithFilters("john_doe_" + uniqueSuffix, null, null, null, null, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getUsername().startsWith("john_doe_" + uniqueSuffix));
    }

    @Test
    void findAllWithFilters_ShouldFilterByRole() {
        Page<User> result = userRepository.findAllWithFilters(null, null, Role.ROLE_ORG_ADMIN, null, null, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getUsername().startsWith("jane_admin_" + uniqueSuffix));
    }

    @Test
    void findAllWithFilters_ShouldFilterByOrganization() {
        Page<User> result = userRepository.findAllWithFilters(null, null, null, null, org.getId(), PageRequest.of(0, 10));
        assertEquals(2, result.getTotalElements());
    }
}
