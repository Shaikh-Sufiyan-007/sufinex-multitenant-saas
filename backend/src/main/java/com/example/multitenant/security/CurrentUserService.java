package com.example.multitenant.security;

import com.example.multitenant.entity.User;
import com.example.multitenant.enums.Role;
import com.example.multitenant.exception.BadRequestException;
import com.example.multitenant.exception.ResourceNotFoundException;
import com.example.multitenant.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("No authenticated security context found");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }
        throw new BadCredentialsException("Invalid authentication principal");
    }

    public Long getCurrentUserId() {
        return getCurrentPrincipal().getId();
    }

    public Long getCurrentTenantId() {
        return getCurrentPrincipal().getTenantId();
    }

    public Long requireCurrentTenantId() {
        Long tenantId = getCurrentTenantId();
        if (tenantId == null) {
            throw new BadRequestException("Current authenticated user is not associated with any tenant");
        }
        return tenantId;
    }

    public String getCurrentTenantSlug() {
        return getCurrentPrincipal().getTenantSlug();
    }

    public Role getCurrentRole() {
        return getCurrentPrincipal().getRole();
    }

    public boolean isTenantAdmin() {
        return getCurrentRole() == Role.TENANT_ADMIN;
    }

    public void verifyTenantAccess(Long targetTenantId) {
        Long userTenantId = requireCurrentTenantId();
        if (!userTenantId.equals(targetTenantId)) {
            throw new AccessDeniedException("Access denied: You cannot access data outside your organization");
        }
    }

    public User getCurrentUserEntity() {
        Long userId = getCurrentUserId();
        return userRepository.findByIdWithTenant(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
