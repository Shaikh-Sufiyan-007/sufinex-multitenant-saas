package com.example.multitenant.controller;

import com.example.multitenant.dto.project.*;
import com.example.multitenant.dto.response.ApiResponse;
import com.example.multitenant.dto.response.PageResponse;
import com.example.multitenant.enums.ProjectPriority;
import com.example.multitenant.enums.ProjectStatus;
import com.example.multitenant.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) ProjectPriority priority,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjectResponse> projectsPage = projectService.getProjects(status, priority, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Projects retrieved successfully", PageResponse.fromPage(projectsPage)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id) {
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success("Project retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectStatusRequest request) {
        ProjectResponse response = projectService.updateProjectStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Project status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.archiveProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project archived successfully", null));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(@PathVariable Long id) {
        List<ProjectMemberResponse> members = projectService.getProjectMembers(id);
        return ResponseEntity.ok(ApiResponse.success("Project members retrieved successfully", members));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addProjectMember(
            @PathVariable Long id,
            @Valid @RequestBody AddProjectMemberRequest request) {
        ProjectMemberResponse member = projectService.addProjectMember(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project member added successfully", member));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<Void>> removeProjectMember(
            @PathVariable Long id,
            @PathVariable Long userId) {
        projectService.removeProjectMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Project member removed successfully", null));
    }

    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<ProjectDashboardResponse>> getProjectDashboard(@PathVariable Long id) {
        ProjectDashboardResponse dashboard = projectService.getProjectDashboard(id);
        return ResponseEntity.ok(ApiResponse.success("Project dashboard statistics retrieved successfully", dashboard));
    }
}
