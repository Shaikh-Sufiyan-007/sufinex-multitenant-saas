package com.example.multitenant.repository;

import com.example.multitenant.entity.Project;
import com.example.multitenant.enums.ProjectPriority;
import com.example.multitenant.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT p FROM Project p WHERE p.tenant.id = :tenantId AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:priority IS NULL OR p.priority = :priority) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Project> searchTenantProjects(@Param("tenantId") Long tenantId,
                                      @Param("status") ProjectStatus status,
                                      @Param("priority") ProjectPriority priority,
                                      @Param("search") String search,
                                      Pageable pageable);

    @Query("SELECT DISTINCT pm.project FROM ProjectMember pm WHERE pm.project.tenant.id = :tenantId AND " +
           "pm.user.id = :userId AND " +
           "(:status IS NULL OR pm.project.status = :status) AND " +
           "(:priority IS NULL OR pm.project.priority = :priority) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(pm.project.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pm.project.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Project> searchUserProjects(@Param("tenantId") Long tenantId,
                                    @Param("userId") Long userId,
                                    @Param("status") ProjectStatus status,
                                    @Param("priority") ProjectPriority priority,
                                    @Param("search") String search,
                                    Pageable pageable);

    List<Project> findByTeamId(Long teamId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, ProjectStatus status);
}
