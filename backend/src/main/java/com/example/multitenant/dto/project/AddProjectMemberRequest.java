package com.example.multitenant.dto.project;

import com.example.multitenant.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public class AddProjectMemberRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private ProjectRole role = ProjectRole.MEMBER;

    public AddProjectMemberRequest() {
    }

    public AddProjectMemberRequest(Long userId, ProjectRole role) {
        this.userId = userId;
        this.role = role != null ? role : ProjectRole.MEMBER;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }
}
