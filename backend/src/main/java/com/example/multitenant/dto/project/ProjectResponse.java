package com.example.multitenant.dto.project;

import com.example.multitenant.enums.ProjectPriority;
import com.example.multitenant.enums.ProjectStatus;

import java.time.Instant;
import java.time.LocalDate;

public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private ProjectPriority priority;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Long teamId;
    private String teamName;
    private Long memberCount;
    private Long taskCount;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;

    public ProjectResponse() {
    }

    public ProjectResponse(Long id, String name, String description, ProjectStatus status,
                           ProjectPriority priority, LocalDate startDate, LocalDate dueDate,
                           Long teamId, String teamName, Long memberCount, Long taskCount,
                           String createdByName, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.teamId = teamId;
        this.teamName = teamName;
        this.memberCount = memberCount;
        this.taskCount = taskCount;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
    }

    public Long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Long taskCount) {
        this.taskCount = taskCount;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
