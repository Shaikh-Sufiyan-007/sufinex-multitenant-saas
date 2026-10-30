package com.example.multitenant.service;

import com.example.multitenant.dto.project.*;
import com.example.multitenant.entity.*;
import com.example.multitenant.enums.*;
import com.example.multitenant.exception.BadRequestException;
import com.example.multitenant.exception.DuplicateResourceException;
import com.example.multitenant.exception.ResourceNotFoundException;
import com.example.multitenant.repository.*;
import com.example.multitenant.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing projects and project memberships within a tenant.
 * Enforces tenant isolation and permission rules (admin and project manager
 * roles).
 */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TeamRepository teamRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public ProjectService(ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            TeamRepository teamRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.teamRepository = teamRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        if (!currentUserService.isTenantAdmin()) {
            throw new AccessDeniedException("Only tenant administrators can create projects");
        }

        Long tenantId = currentUserService.requireCurrentTenantId();
        User currentUser = currentUserService.getCurrentUserEntity();

        validateDates(request.getStartDate(), request.getDueDate());

        Team team = resolveTeam(request.getTeamId(), tenantId);

        Project project = new Project(
                request.getName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getStatus() != null ? request.getStatus() : ProjectStatus.ACTIVE,
                request.getPriority() != null ? request.getPriority() : ProjectPriority.MEDIUM,
                request.getStartDate(),
                request.getDueDate(),
                currentUser.getTenant(),
                team,
                currentUser);
        Project savedProject = projectRepository.save(project);

        // Project creator is automatically registered as a MANAGER
        projectMemberRepository.save(new ProjectMember(savedProject, currentUser, ProjectRole.MANAGER));

        return mapToProjectResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(ProjectStatus status, ProjectPriority priority,
            String search, Pageable pageable) {
        Long tenantId = currentUserService.requireCurrentTenantId();

        Page<Project> page;
        if (currentUserService.isTenantAdmin()) {
            page = projectRepository.searchTenantProjects(tenantId, status, priority, search, pageable);
        } else {
            Long currentUserId = currentUserService.getCurrentUserId();
            page = projectRepository.searchUserProjects(tenantId, currentUserId, status, priority, search, pageable);
        }

        return page.map(this::mapToProjectResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Project project = findProjectOrThrow(projectId, tenantId);

        checkUserCanViewProject(project.getId(), currentUserId);

        return mapToProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Project project = findProjectOrThrow(projectId, tenantId);

        requireProjectWriteAccess(project, currentUserId);
        requireNotArchived(project);
        validateDates(request.getStartDate(), request.getDueDate());

        Team team = resolveTeam(request.getTeamId(), tenantId);

        project.setName(request.getName().trim());
        project.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        }
        project.setStartDate(request.getStartDate());
        project.setDueDate(request.getDueDate());
        project.setTeam(team);

        return mapToProjectResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateProjectStatus(Long projectId, UpdateProjectStatusRequest request) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Project project = findProjectOrThrow(projectId, tenantId);

        requireProjectWriteAccess(project, currentUserId);

        project.setStatus(request.getStatus());
        return mapToProjectResponse(projectRepository.save(project));
    }

    @Transactional
    public void archiveProject(Long projectId) {
        if (!currentUserService.isTenantAdmin()) {
            throw new AccessDeniedException("Only tenant administrators can archive projects");
        }

        Long tenantId = currentUserService.requireCurrentTenantId();
        Project project = findProjectOrThrow(projectId, tenantId);

        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Project project = findProjectOrThrow(projectId, tenantId);

        checkUserCanViewProject(project.getId(), currentUserId);

        List<ProjectMember> members = projectMemberRepository.findByProjectIdWithUser(project.getId());
        List<ProjectMemberResponse> responseList = new ArrayList<>();
        for (ProjectMember member : members) {
            responseList.add(mapToProjectMemberResponse(member));
        }
        return responseList;
    }

    @Transactional
    public ProjectMemberResponse addProjectMember(Long projectId, AddProjectMemberRequest request) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Project project = findProjectOrThrow(projectId, tenantId);

        requireProjectWriteAccess(project, currentUserId);
        requireNotArchived(project);

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (targetUser.getTenant() == null || !targetUser.getTenant().getId().equals(tenantId)) {
            throw new BadRequestException("Target user does not belong to your organisation");
        }

        if (projectMemberRepository.existsByProjectIdAndUserId(project.getId(), targetUser.getId())) {
            throw new DuplicateResourceException("User is already a member of this project");
        }

        ProjectRole role = request.getRole() != null ? request.getRole() : ProjectRole.MEMBER;
        ProjectMember saved = projectMemberRepository.save(new ProjectMember(project, targetUser, role));
        return mapToProjectMemberResponse(saved);
    }

    @Transactional
    public void removeProjectMember(Long projectId, Long userId) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Project project = findProjectOrThrow(projectId, tenantId);

        requireProjectWriteAccess(project, currentUserId);
        requireNotArchived(project);

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(project.getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this project"));

        projectMemberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public ProjectDashboardResponse getProjectDashboard(Long projectId) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Project project = findProjectOrThrow(projectId, tenantId);

        checkUserCanViewProject(project.getId(), currentUserId);

        long totalTasks = taskRepository.countByProjectId(projectId);
        long todoTasks = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.IN_PROGRESS);
        long completedTasks = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.COMPLETED);
        long highPriority = taskRepository.countByProjectIdAndPriority(projectId, TaskPriority.HIGH);
        long overdue = taskRepository.countByProjectIdAndStatusNotAndDueDateBefore(
                projectId, TaskStatus.COMPLETED, LocalDate.now());

        return new ProjectDashboardResponse(totalTasks, todoTasks, inProgressTasks,
                completedTasks, highPriority, overdue);
    }

    // ── Helper methods ──────────────────────────────────────────────────────

    private Project findProjectOrThrow(Long projectId, Long tenantId) {
        return projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }

    private void checkUserCanViewProject(Long projectId, Long currentUserId) {
        if (currentUserService.isTenantAdmin()) {
            return;
        }
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId)) {
            throw new AccessDeniedException("You are not authorised to access this project");
        }
    }

    private void requireProjectWriteAccess(Project project, Long currentUserId) {
        if (currentUserService.isTenantAdmin()) {
            return;
        }

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(project.getId(), currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project"));

        if (member.getRole() != ProjectRole.MANAGER) {
            throw new AccessDeniedException("Only project managers or tenant admins can modify project details");
        }
    }

    private void requireNotArchived(Project project) {
        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new BadRequestException("Archived project cannot be modified");
        }
    }

    private void validateDates(LocalDate startDate, LocalDate dueDate) {
        if (startDate != null && dueDate != null && dueDate.isBefore(startDate)) {
            throw new BadRequestException("Due date cannot be before start date");
        }
    }

    private Team resolveTeam(Long teamId, Long tenantId) {
        if (teamId == null) {
            return null;
        }
        return teamRepository.findByIdAndTenantId(teamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found in your organisation"));
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        long memberCount = projectMemberRepository.countByProjectId(project.getId());
        long taskCount = taskRepository.countByProjectId(project.getId());
        String teamName = project.getTeam() != null ? project.getTeam().getName() : null;
        Long teamId = project.getTeam() != null ? project.getTeam().getId() : null;
        String createdByName = project.getCreatedBy() != null ? project.getCreatedBy().getName() : "System";

        return new ProjectResponse(
                project.getId(), project.getName(), project.getDescription(),
                project.getStatus(), project.getPriority(),
                project.getStartDate(), project.getDueDate(),
                teamId, teamName, memberCount, taskCount,
                createdByName, project.getCreatedAt(), project.getUpdatedAt());
    }

    private ProjectMemberResponse mapToProjectMemberResponse(ProjectMember member) {
        User u = member.getUser();
        return new ProjectMemberResponse(
                member.getId(), u.getId(), u.getEmail(), u.getName(),
                u.getRole(), member.getRole(), member.getAddedAt());
    }
}
