package com.example.multitenant.repository;

import com.example.multitenant.entity.Tenant;
import com.example.multitenant.enums.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    long countByStatus(TenantStatus status);

    @Query("SELECT t FROM Tenant t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Tenant> searchTenants(
            @Param("search") String search,
            @Param("status") TenantStatus status,
            Pageable pageable
    );
}
