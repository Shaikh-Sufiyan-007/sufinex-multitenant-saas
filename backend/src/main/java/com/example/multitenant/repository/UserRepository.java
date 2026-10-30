package com.example.multitenant.repository;

import com.example.multitenant.entity.User;
import com.example.multitenant.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tenant WHERE u.email = :email")
    List<User> findAllByEmailWithTenant(@Param("email") String email);

    @Query("SELECT u FROM User u JOIN FETCH u.tenant WHERE u.email = :email AND u.tenant.slug = :tenantSlug")
    Optional<User> findByEmailAndTenantSlug(@Param("email") String email, @Param("tenantSlug") String tenantSlug);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tenant WHERE u.id = :id")
    Optional<User> findByIdWithTenant(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tenant WHERE u.id = :id AND u.tenant.id = :tenantId")
    Optional<User> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    boolean existsByEmailAndTenantId(String email, Long tenantId);

    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND " +
           "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchTenantUsers(
            @Param("tenantId") Long tenantId,
            @Param("search") String search,
            Pageable pageable
    );

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, UserStatus status);

    long countByStatus(UserStatus status);
}
