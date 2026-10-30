package com.example.multitenant.repository;

import com.example.multitenant.entity.Task;
import com.example.multitenant.enums.TaskPriority;
import com.example.multitenant.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT t FROM Task t " +
           "LEFT JOIN FETCH t.assignedTo " +
           "LEFT JOIN FETCH t.createdBy " +
           "WHERE t.project.id = :projectId AND t.tenant.id = :tenantId AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:assigneeId IS NULL OR (t.assignedTo IS NOT NULL AND t.assignedTo.id = :assigneeId)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Task> searchProjectTasks(@Param("tenantId") Long tenantId,
                                 @Param("projectId") Long projectId,
                                 @Param("status") TaskStatus status,
                                 @Param("priority") TaskPriority priority,
                                 @Param("assigneeId") Long assigneeId,
                                 @Param("search") String search,
                                 Pageable pageable);

    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.project " +
           "LEFT JOIN FETCH t.createdBy " +
           "WHERE t.tenant.id = :tenantId AND t.assignedTo.id = :userId AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.project.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Task> searchUserAssignedTasks(@Param("tenantId") Long tenantId,
                                      @Param("userId") Long userId,
                                      @Param("status") TaskStatus status,
                                      @Param("search") String search,
                                      Pageable pageable);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, TaskStatus status);

    long countByProjectIdAndPriority(Long projectId, TaskPriority priority);

    long countByProjectIdAndStatusNotAndDueDateBefore(Long projectId, TaskStatus status, LocalDate today);

    void deleteByProjectId(Long projectId);
}
