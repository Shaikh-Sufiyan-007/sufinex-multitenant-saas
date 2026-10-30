package com.example.multitenant.repository;

import com.example.multitenant.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.user u WHERE pm.project.id = :projectId ORDER BY pm.addedAt ASC")
    List<ProjectMember> findByProjectIdWithUser(@Param("projectId") Long projectId);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    void deleteByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
