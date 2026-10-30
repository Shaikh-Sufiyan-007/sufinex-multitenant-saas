package com.example.multitenant.dto.auth;

import com.example.multitenant.entity.User;
import com.example.multitenant.enums.Role;
import com.example.multitenant.enums.UserStatus;
import com.example.multitenant.security.UserPrincipal;

import java.time.Instant;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
    private TenantResponse tenant;
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, Role role, UserStatus status, TenantResponse tenant, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.tenant = tenant;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getTenant() != null ? TenantResponse.fromEntity(user.getTenant()) : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static UserResponse fromPrincipal(UserPrincipal principal) {
        if (principal == null) {
            return null;
        }
        TenantResponse tenantResponse = null;
        if (principal.getTenantId() != null) {
            tenantResponse = new TenantResponse(
                    principal.getTenantId(),
                    principal.getTenantName(),
                    principal.getTenantSlug(),
                    null,
                    null,
                    null
            );
        }
        return new UserResponse(
                principal.getId(),
                principal.getName(),
                principal.getEmail(),
                principal.getRole(),
                principal.getStatus(),
                tenantResponse,
                null,
                null
        );
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public TenantResponse getTenant() {
        return tenant;
    }

    public void setTenant(TenantResponse tenant) {
        this.tenant = tenant;
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
