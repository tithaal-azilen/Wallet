package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class OrganizationRepositoryTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @BeforeEach
    void setUp() {
        Organization org1 = Organization.builder()
                .name("Alpha Corp")
                .orgCode("ALPHA")
                .status(OrganizationStatus.ACTIVE)
                .createdAt(Instant.now().minus(10, ChronoUnit.DAYS))
                .build();

        Organization org2 = Organization.builder()
                .name("Beta Industries")
                .orgCode("BETA")
                .status(OrganizationStatus.SUSPENDED)
                .createdAt(Instant.now())
                .build();

        organizationRepository.save(org1);
        organizationRepository.save(org2);
    }

    @Test
    void findAllWithFilters_ShouldFilterByNamePartial() {
        Page<Organization> result = organizationRepository.findAllWithFilters("Alpha", null, null, null, null, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("ALPHA", result.getContent().get(0).getOrgCode());
    }

    @Test
    void findAllWithFilters_ShouldFilterByStatus() {
        Page<Organization> result = organizationRepository.findAllWithFilters(null, null, OrganizationStatus.SUSPENDED, null, null, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("BETA", result.getContent().get(0).getOrgCode());
    }

    @Test
    void findAllWithFilters_ShouldFilterByDateRange() {
        Instant start = Instant.now().minus(5, ChronoUnit.DAYS);
        Page<Organization> result = organizationRepository.findAllWithFilters(null, null, null, start, null, PageRequest.of(0, 10));
        // Only Beta was created in the last 5 days
        assertEquals(1, result.getTotalElements());
        assertEquals("BETA", result.getContent().get(0).getOrgCode());
    }
}
