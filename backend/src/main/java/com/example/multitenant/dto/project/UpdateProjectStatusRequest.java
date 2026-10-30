package com.example.multitenant.dto.project;

import com.example.multitenant.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateProjectStatusRequest {

    @NotNull(message = "Project status is required")
    private ProjectStatus status;

    public UpdateProjectStatusRequest() {
    }

    public UpdateProjectStatusRequest(ProjectStatus status) {
        this.status = status;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}
