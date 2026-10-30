package com.example.multitenant.dto.project;

import com.example.multitenant.enums.ProjectPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 150, message = "Project name must be between 2 and 150 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private ProjectPriority priority = ProjectPriority.MEDIUM;

    private LocalDate startDate;

    private LocalDate dueDate;

    private Long teamId;

    public UpdateProjectRequest() {
    }

    public UpdateProjectRequest(String name, String description, ProjectPriority priority,
                                LocalDate startDate, LocalDate dueDate, Long teamId) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectPriority getPriority() {
        return priority;
    }

    public void setPriority(ProjectPriority priority) {
        this.priority = priority;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}
