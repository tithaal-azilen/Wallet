package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByOrgCode(String orgCode);

    boolean existsByOrgCode(String orgCode);

    boolean existsByName(String name);

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Organization o WHERE " +
            "(:name IS NULL OR o.name ILIKE %:name%) AND " +
            "(:orgCode IS NULL OR o.orgCode = :orgCode) AND " +
            "((:status IS NULL AND o.status != com.Tithaal.Wallet.entity.OrganizationStatus.DELETED) OR (o.status = :status)) AND " +
            "(cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate) AND " +
            "(cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)")
    org.springframework.data.domain.Page<Organization> findAllWithFilters(
            @org.springframework.data.repository.query.Param("name") String name,
            @org.springframework.data.repository.query.Param("orgCode") String orgCode,
            @org.springframework.data.repository.query.Param("status") com.Tithaal.Wallet.entity.OrganizationStatus status,
            @org.springframework.data.repository.query.Param("startDate") java.time.Instant startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.Instant endDate,
            org.springframework.data.domain.Pageable pageable);
}
