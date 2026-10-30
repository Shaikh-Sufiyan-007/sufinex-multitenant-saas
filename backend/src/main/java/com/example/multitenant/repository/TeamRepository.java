package com.example.multitenant.repository;

import com.example.multitenant.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByNameIgnoreCaseAndTenantId(String name, Long tenantId);

    boolean existsByNameIgnoreCaseAndTenantIdAndIdNot(String name, Long tenantId, Long id);

    @Query("SELECT t FROM Team t WHERE t.tenant.id = :tenantId AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Team> searchTenantTeams(@Param("tenantId") Long tenantId,
                                @Param("search") String search,
                                Pageable pageable);

    @Query("SELECT DISTINCT tm.team FROM TeamMember tm WHERE tm.team.tenant.id = :tenantId AND " +
           "tm.user.id = :userId AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(tm.team.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(tm.team.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Team> searchUserTeams(@Param("tenantId") Long tenantId,
                              @Param("userId") Long userId,
                              @Param("search") String search,
                              Pageable pageable);

    long countByTenantId(Long tenantId);
}
