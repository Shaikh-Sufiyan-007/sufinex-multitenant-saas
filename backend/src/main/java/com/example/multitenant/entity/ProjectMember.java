package com.example.multitenant.entity;

import com.example.multitenant.enums.ProjectRole;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "project_members",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_project_member", columnNames = {"project_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_proj_member_project", columnList = "project_id"),
        @Index(name = "idx_proj_member_user", columnList = "user_id")
    }
)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectRole role = ProjectRole.MEMBER;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    public ProjectMember() {
    }

    public ProjectMember(Project project, User user, ProjectRole role) {
        this.project = project;
        this.user = user;
        this.role = role != null ? role : ProjectRole.MEMBER;
    }

    @PrePersist
    protected void onCreate() {
        if (this.addedAt == null) {
            this.addedAt = Instant.now();
        }
        if (this.role == null) {
            this.role = ProjectRole.MEMBER;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectMember that = (ProjectMember) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
