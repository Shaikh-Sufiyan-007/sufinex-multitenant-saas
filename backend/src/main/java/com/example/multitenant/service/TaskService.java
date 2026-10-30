package com.example.multitenant.service;

import com.example.multitenant.dto.task.*;
import com.example.multitenant.entity.*;
import com.example.multitenant.enums.*;
import com.example.multitenant.exception.BadRequestException;
import com.example.multitenant.exception.ResourceNotFoundException;
import com.example.multitenant.repository.*;
import com.example.multitenant.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public TaskService(TaskRepository taskRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public TaskResponse createTask(Long projectId, CreateTaskRequest request) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();
        User currentUser = currentUserService.getCurrentUserEntity();

        Project project = findProjectOrThrow(projectId, tenantId);
        requireNotArchived(project);

        if (!currentUserService.isTenantAdmin()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId)) {
                throw new AccessDeniedException("You are not authorised to create tasks in this project");
            }
        }

        User assignee = null;
        if (request.getAssignedToUserId() != null) {
            assignee = resolveAssignee(project, tenantId, request.getAssignedToUserId());
        }

        Task task = new Task(
                request.getTitle().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                TaskStatus.TODO,
                request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM,
                request.getDueDate(),
                currentUser.getTenant(),
                project,
                assignee,
                currentUser);

        return mapToTaskResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getProjectTasks(Long projectId, TaskStatus status, TaskPriority priority,
            Long assigneeId, String search, Pageable pageable) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Project project = findProjectOrThrow(projectId, tenantId);

        if (!currentUserService.isTenantAdmin()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId)) {
                throw new AccessDeniedException("You are not authorised to view tasks for this project");
            }
        }

        return taskRepository.searchProjectTasks(tenantId, project.getId(),
                status, priority, assigneeId, search, pageable).map(this::mapToTaskResponse);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getMyAssignedTasks(TaskStatus status, String search, Pageable pageable) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();
        return taskRepository.searchUserAssignedTasks(tenantId, currentUserId, status, search, pageable)
                .map(this::mapToTaskResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Task task = findTaskOrThrow(taskId, tenantId);

        if (!currentUserService.isTenantAdmin()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), currentUserId)) {
                throw new AccessDeniedException("You are not authorised to view this task");
            }
        }

        return mapToTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Task task = findTaskOrThrow(taskId, tenantId);

        requireNotArchived(task.getProject());
        requireTaskWriteAccess(task, currentUserId);

        User assignee = null;
        if (request.getAssignedToUserId() != null) {
            assignee = resolveAssignee(task.getProject(), tenantId, request.getAssignedToUserId());
        }

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setDueDate(request.getDueDate());
        task.setAssignedTo(assignee);

        return mapToTaskResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Task task = findTaskOrThrow(taskId, tenantId);

        requireNotArchived(task.getProject());

        if (!currentUserService.isTenantAdmin()) {
            boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUserId);
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(),
                    currentUserId);
            if (!isAssignee && !isMember) {
                throw new AccessDeniedException("You are not authorised to update status of this task");
            }
        }

        task.setStatus(request.getStatus());
        return mapToTaskResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse assignTask(Long taskId, AssignTaskRequest request) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Task task = findTaskOrThrow(taskId, tenantId);

        requireNotArchived(task.getProject());
        requireTaskAssignAccess(task, currentUserId);

        User assignee = null;
        if (request.getAssignedToUserId() != null) {
            assignee = resolveAssignee(task.getProject(), tenantId, request.getAssignedToUserId());
        }

        task.setAssignedTo(assignee);
        return mapToTaskResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Task task = findTaskOrThrow(taskId, tenantId);

        requireNotArchived(task.getProject());
        requireTaskAssignAccess(task, currentUserId);

        taskRepository.delete(task);
    }

    // ── Helper methods ──────────────────────────────────────────────────────

    private Task findTaskOrThrow(Long taskId, Long tenantId) {
        return taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private Project findProjectOrThrow(Long projectId, Long tenantId) {
        return projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }

    private User resolveAssignee(Project project, Long tenantId, Long assigneeUserId) {
        User user = userRepository.findById(assigneeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found with id: " + assigneeUserId));

        if (user.getTenant() == null || !user.getTenant().getId().equals(tenantId)) {
            throw new BadRequestException("Cannot assign task to a user outside your organisation");
        }

        if (!projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user.getId())) {
            throw new BadRequestException("Assignee must be a member of the project");
        }

        return user;
    }

    private void requireTaskWriteAccess(Task task, Long currentUserId) {
        if (currentUserService.isTenantAdmin()) {
            return;
        }

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(task.getProject().getId(), currentUserId)
                .orElseThrow(() -> new AccessDeniedException(
                        "You are not authorised to modify tasks in this project"));

        boolean isManager = member.getRole() == ProjectRole.MANAGER;
        boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUserId);

        if (!isManager && !isAssignee) {
            throw new AccessDeniedException(
                    "You can only edit tasks assigned to you, or must be a project manager");
        }
    }

    private void requireTaskAssignAccess(Task task, Long currentUserId) {
        if (currentUserService.isTenantAdmin()) {
            return;
        }

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(task.getProject().getId(), currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project"));

        if (member.getRole() != ProjectRole.MANAGER) {
            throw new AccessDeniedException(
                    "Only project managers or tenant admins can assign/delete tasks");
        }
    }

    private void requireNotArchived(Project project) {
        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new BadRequestException("Tasks in an archived project cannot be modified");
        }
    }

    private TaskResponse mapToTaskResponse(Task task) {
        Long assignedId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
        String assignedName = task.getAssignedTo() != null ? task.getAssignedTo().getName() : null;
        String assignedEmail = task.getAssignedTo() != null ? task.getAssignedTo().getEmail() : null;
        Long createdById = task.getCreatedBy() != null ? task.getCreatedBy().getId() : null;
        String createdByName = task.getCreatedBy() != null ? task.getCreatedBy().getName() : null;

        return new TaskResponse(
                task.getId(), task.getTitle(), task.getDescription(),
                task.getStatus(), task.getPriority(), task.getDueDate(),
                task.getProject().getId(), task.getProject().getName(),
                assignedId, assignedName, assignedEmail,
                createdById, createdByName,
                task.getCreatedAt(), task.getUpdatedAt());
    }
}
