package com.example.multitenant.controller;

import com.example.multitenant.dto.response.ApiResponse;
import com.example.multitenant.dto.response.PageResponse;
import com.example.multitenant.dto.team.*;
import com.example.multitenant.service.TeamService;
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
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        TeamResponse response = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<PageResponse<TeamResponse>>> getTeams(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TeamResponse> teamsPage = teamService.getTeams(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Teams retrieved successfully", PageResponse.fromPage(teamsPage)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(@PathVariable Long id) {
        TeamResponse response = teamService.getTeamById(id);
        return ResponseEntity.ok(ApiResponse.success("Team retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeamRequest request) {
        TeamResponse response = teamService.updateTeam(id, request);
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(@PathVariable Long id) {
        List<TeamMemberResponse> members = teamService.getTeamMembers(id);
        return ResponseEntity.ok(ApiResponse.success("Team members retrieved successfully", members));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> addTeamMember(
            @PathVariable Long id,
            @Valid @RequestBody AddTeamMemberRequest request) {
        TeamMemberResponse member = teamService.addTeamMember(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team member added successfully", member));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeTeamMember(
            @PathVariable Long id,
            @PathVariable Long userId) {
        teamService.removeTeamMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Team member removed successfully", null));
    }
}
