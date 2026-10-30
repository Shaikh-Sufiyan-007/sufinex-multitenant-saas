package com.example.multitenant.repository;

import com.example.multitenant.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user u WHERE tm.team.id = :teamId ORDER BY tm.joinedAt ASC")
    List<TeamMember> findByTeamIdWithUser(@Param("teamId") Long teamId);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    void deleteByTeamIdAndUserId(Long teamId, Long userId);

    long countByTeamId(Long teamId);

    void deleteByTeamId(Long teamId);
}
