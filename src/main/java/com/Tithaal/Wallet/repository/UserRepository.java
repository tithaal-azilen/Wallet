package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    org.springframework.data.domain.Page<User> findByOrganizationId(Long organizationId,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u " +
            "LEFT JOIN u.organization o WHERE " +
            "(:username IS NULL OR u.username ILIKE %:username%) AND " +
            "(:email IS NULL OR u.email ILIKE %:email%) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "((:status IS NULL AND u.status != com.Tithaal.Wallet.entity.UserStatus.DELETED) OR (u.status = :status)) AND " +
            "(:organizationId IS NULL OR o.id = :organizationId)")
    org.springframework.data.domain.Page<User> findAllWithFilters(
            @org.springframework.data.repository.query.Param("username") String username,
            @org.springframework.data.repository.query.Param("email") String email,
            @org.springframework.data.repository.query.Param("role") com.Tithaal.Wallet.entity.Role role,
            @org.springframework.data.repository.query.Param("status") com.Tithaal.Wallet.entity.UserStatus status,
            @org.springframework.data.repository.query.Param("organizationId") Long organizationId,
            org.springframework.data.domain.Pageable pageable);
}
