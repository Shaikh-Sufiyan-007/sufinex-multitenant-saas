package com.example.multitenant.service;

import com.example.multitenant.dto.team.*;
import com.example.multitenant.entity.Team;
import com.example.multitenant.entity.TeamMember;
import com.example.multitenant.entity.User;
import com.example.multitenant.exception.BadRequestException;
import com.example.multitenant.exception.DuplicateResourceException;
import com.example.multitenant.exception.ResourceNotFoundException;
import com.example.multitenant.repository.ProjectRepository;
import com.example.multitenant.repository.TeamMemberRepository;
import com.example.multitenant.repository.TeamRepository;
import com.example.multitenant.repository.UserRepository;
import com.example.multitenant.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public TeamService(TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request) {
        requireTenantAdmin();

        Long tenantId = currentUserService.requireCurrentTenantId();
        User currentUser = currentUserService.getCurrentUserEntity();

        String teamName = request.getName().trim();
        if (teamRepository.existsByNameIgnoreCaseAndTenantId(teamName, tenantId)) {
            throw new DuplicateResourceException(
                    "A team named '" + teamName + "' already exists in your organisation");
        }

        Team team = new Team(
                teamName,
                request.getDescription() != null ? request.getDescription().trim() : null,
                currentUser.getTenant(),
                currentUser);
        Team savedTeam = teamRepository.save(team);

        // Creator automatically becomes the initial team member
        teamMemberRepository.save(new TeamMember(savedTeam, currentUser));

        return mapToTeamResponse(savedTeam);
    }

    @Transactional(readOnly = true)
    public Page<TeamResponse> getTeams(String search, Pageable pageable) {
        Long tenantId = currentUserService.requireCurrentTenantId();

        Page<Team> page;
        if (currentUserService.isTenantAdmin()) {
            page = teamRepository.searchTenantTeams(tenantId, search, pageable);
        } else {
            Long currentUserId = currentUserService.getCurrentUserId();
            page = teamRepository.searchUserTeams(tenantId, currentUserId, search, pageable);
        }

        return page.map(this::mapToTeamResponse);
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long teamId) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Team team = findTeamOrThrow(teamId, tenantId);

        if (!currentUserService.isTenantAdmin()) {
            if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, currentUserId)) {
                throw new AccessDeniedException("You are not a member of this team");
            }
        }

        return mapToTeamResponse(team);
    }

    @Transactional
    public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request) {
        requireTenantAdmin();

        Long tenantId = currentUserService.requireCurrentTenantId();
        Team team = findTeamOrThrow(teamId, tenantId);

        String newName = request.getName().trim();
        if (teamRepository.existsByNameIgnoreCaseAndTenantIdAndIdNot(newName, tenantId, teamId)) {
            throw new DuplicateResourceException("A team named '" + newName + "' already exists in your organisation");
        }

        team.setName(newName);
        team.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        return mapToTeamResponse(teamRepository.save(team));
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        requireTenantAdmin();

        Long tenantId = currentUserService.requireCurrentTenantId();
        Team team = findTeamOrThrow(teamId, tenantId);

        teamMemberRepository.deleteByTeamId(team.getId());
        teamRepository.delete(team);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Team team = findTeamOrThrow(teamId, tenantId);

        if (!currentUserService.isTenantAdmin()) {
            Long currentUserId = currentUserService.getCurrentUserId();
            if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, currentUserId)) {
                throw new AccessDeniedException("You are not authorised to view members of this team");
            }
        }

        List<TeamMember> members = teamMemberRepository.findByTeamIdWithUser(team.getId());
        List<TeamMemberResponse> responseList = new ArrayList<>();
        for (TeamMember member : members) {
            responseList.add(mapToTeamMemberResponse(member));
        }
        return responseList;
    }

    @Transactional
    public TeamMemberResponse addTeamMember(Long teamId, AddTeamMemberRequest request) {
        requireTenantAdmin();

        Long tenantId = currentUserService.requireCurrentTenantId();
        Team team = findTeamOrThrow(teamId, tenantId);

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (targetUser.getTenant() == null || !targetUser.getTenant().getId().equals(tenantId)) {
            throw new BadRequestException("Target user does not belong to your organisation");
        }

        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), targetUser.getId())) {
            throw new DuplicateResourceException("User is already a member of this team");
        }

        TeamMember saved = teamMemberRepository.save(new TeamMember(team, targetUser));
        return mapToTeamMemberResponse(saved);
    }

    @Transactional
    public void removeTeamMember(Long teamId, Long userId) {
        requireTenantAdmin();

        Long tenantId = currentUserService.requireCurrentTenantId();
        Team team = findTeamOrThrow(teamId, tenantId);

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(team.getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this team"));

        teamMemberRepository.delete(member);
    }

    // ── Helper methods ──────────────────────────────────────────────────────

    private Team findTeamOrThrow(Long teamId, Long tenantId) {
        return teamRepository.findByIdAndTenantId(teamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
    }

    private void requireTenantAdmin() {
        if (!currentUserService.isTenantAdmin()) {
            throw new AccessDeniedException("Only tenant administrators can perform this operation");
        }
    }

    private TeamResponse mapToTeamResponse(Team team) {
        long memberCount = teamMemberRepository.countByTeamId(team.getId());
        long projectCount = projectRepository.findByTeamId(team.getId()).size();
        String createdByName = team.getCreatedBy() != null ? team.getCreatedBy().getName() : "System";

        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                memberCount,
                projectCount,
                createdByName,
                team.getCreatedAt(),
                team.getUpdatedAt());
    }

    private TeamMemberResponse mapToTeamMemberResponse(TeamMember member) {
        User u = member.getUser();
        return new TeamMemberResponse(
                member.getId(),
                u.getId(),
                u.getEmail(),
                u.getName(),
                u.getRole(),
                member.getJoinedAt());
    }
}
