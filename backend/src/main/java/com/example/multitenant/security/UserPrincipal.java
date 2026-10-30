package com.example.multitenant.security;

import com.example.multitenant.entity.User;
import com.example.multitenant.enums.Role;
import com.example.multitenant.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;
    private final UserStatus status;
    private final Long tenantId;
    private final String tenantName;
    private final String tenantSlug;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id,
                         String name,
                         String email,
                         String password,
                         Role role,
                         UserStatus status,
                         Long tenantId,
                         String tenantName,
                         String tenantSlug,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.tenantSlug = tenantSlug;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        
        Long tenantId = null;
        String tenantName = null;
        String tenantSlug = null;

        if (user.getTenant() != null) {
            tenantId = user.getTenant().getId();
            tenantName = user.getTenant().getName();
            tenantSlug = user.getTenant().getSlug();
        }

        return new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getStatus() != null ? user.getStatus() : UserStatus.ACTIVE,
                tenantId,
                tenantName,
                tenantSlug,
                Collections.singletonList(authority)
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getTenantSlug() {
        return tenantSlug;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
