package com.example.multitenant.dto.team;

import com.example.multitenant.enums.Role;
import java.time.Instant;

public class TeamMemberResponse {

    private Long id;
    private Long userId;
    private String email;
    private String name;
    private Role role;
    private Instant joinedAt;

    public TeamMemberResponse() {
    }

    public TeamMemberResponse(Long id, Long userId, String email, String name, Role role, Instant joinedAt) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}
