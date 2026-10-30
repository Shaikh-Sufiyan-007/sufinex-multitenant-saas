package com.example.multitenant.controller;

import com.example.multitenant.dto.response.ApiResponse;
import com.example.multitenant.dto.response.PageResponse;
import com.example.multitenant.dto.task.*;
import com.example.multitenant.enums.TaskPriority;
import com.example.multitenant.enums.TaskStatus;
import com.example.multitenant.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/api/projects/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", response));
    }

    @GetMapping("/api/projects/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getProjectTasks(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TaskResponse> tasksPage = taskService.getProjectTasks(projectId, status, priority, assigneeId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Project tasks retrieved successfully", PageResponse.fromPage(tasksPage)));
    }

    @GetMapping("/api/tasks/my-tasks")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getMyAssignedTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TaskResponse> tasksPage = taskService.getMyAssignedTasks(status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("My tasks retrieved successfully", PageResponse.fromPage(tasksPage)));
    }

    @GetMapping("/api/tasks/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", response));
    }

    @PutMapping("/api/tasks/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response));
    }

    @PatchMapping("/api/tasks/{id}/status")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        TaskResponse response = taskService.updateTaskStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully", response));
    }

    @PatchMapping("/api/tasks/{id}/assignee")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long id,
            @RequestBody AssignTaskRequest request) {
        TaskResponse response = taskService.assignTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task assignee updated successfully", response));
    }

    @DeleteMapping("/api/tasks/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }
}
