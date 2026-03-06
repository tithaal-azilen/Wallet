package com.Tithaal.Wallet.seeder;

import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.entity.Role;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.UserStatus;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting database seeding process...");
        seedSuperAdmin();
        log.info("Database seeding process completed.");
    }

    private void seedSuperAdmin() {
        String superAdminEmail = "SuperAdmin@PlatformAdmin.com";
        String superAdminUsername = "SuperAdmin";
        String platformOrgCode = "PLATFORM_ADMIN";

        if (userRepository.existsByEmail(superAdminEmail)) {
            log.info("Super Admin already exists. Skipping seeding.");
            return;
        }

        log.info("Creating PlatformAdmin organization and SuperAdmin user...");

        // Create PlatformAdmin Organization
        Organization superOrg = organizationRepository.findByOrgCode(platformOrgCode)
                .orElseGet(() -> {
                    Organization org = Organization.builder()
                            .name("PlatformAdmin")
                            .orgCode(platformOrgCode)
                            .status(OrganizationStatus.ACTIVE)
                            .createdAt(Instant.now())
                            .build();
                    return organizationRepository.save(org);
                });

        // Create SuperAdmin User
        User superAdmin = User.builder()
                .username(superAdminUsername)
                .email(superAdminEmail)
                .passwordHash(passwordEncoder.encode(superAdminUsername)) // Password same as username
                .role(Role.ROLE_SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .organization(superOrg)
                .createdAt(Instant.now())
                .build();

        userRepository.save(superAdmin);
        log.info("Super Admin user created successfully.");
    }
}
