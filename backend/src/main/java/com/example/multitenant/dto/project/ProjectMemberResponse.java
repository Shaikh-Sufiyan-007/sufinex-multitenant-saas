package com.example.multitenant.dto.project;

import com.example.multitenant.enums.ProjectRole;
import com.example.multitenant.enums.Role;
import java.time.Instant;

public class ProjectMemberResponse {

    private Long id;
    private Long userId;
    private String email;
    private String name;
    private Role userRole;
    private ProjectRole projectRole;
    private Instant addedAt;

    public ProjectMemberResponse() {
    }

    public ProjectMemberResponse(Long id, Long userId, String email, String name,
                                 Role userRole, ProjectRole projectRole, Instant addedAt) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.userRole = userRole;
        this.projectRole = projectRole;
        this.addedAt = addedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Role getUserRole() { return userRole; }
    public void setUserRole(Role userRole) { this.userRole = userRole; }

    public ProjectRole getProjectRole() { return projectRole; }
    public void setProjectRole(ProjectRole projectRole) { this.projectRole = projectRole; }

    public Instant getAddedAt() { return addedAt; }
    public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
}
