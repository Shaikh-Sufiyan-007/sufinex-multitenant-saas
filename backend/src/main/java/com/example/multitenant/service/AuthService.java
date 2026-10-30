package com.example.multitenant.service;

import com.example.multitenant.dto.auth.AuthResponse;
import com.example.multitenant.dto.auth.LoginRequest;
import com.example.multitenant.dto.auth.RegisterTenantRequest;
import com.example.multitenant.dto.auth.UserResponse;
import com.example.multitenant.entity.Tenant;
import com.example.multitenant.entity.User;
import com.example.multitenant.enums.Role;
import com.example.multitenant.enums.TenantStatus;
import com.example.multitenant.enums.UserStatus;
import com.example.multitenant.exception.BadRequestException;
import com.example.multitenant.exception.DuplicateResourceException;
import com.example.multitenant.exception.ResourceNotFoundException;
import com.example.multitenant.repository.TenantRepository;
import com.example.multitenant.repository.UserRepository;
import com.example.multitenant.security.JwtService;
import com.example.multitenant.security.UserPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registerTenant(RegisterTenantRequest request) {
        String cleanSlug = request.getTenantSlug().trim().toLowerCase();
        validateTenantNotExists(cleanSlug, request.getTenantName().trim());

        Tenant tenant = createAndSaveTenant(request.getTenantName().trim(), cleanSlug);

        String email = request.getEmail().trim().toLowerCase();
        validateEmailNotInTenant(email, tenant.getId());

        User user = createAndSaveAdminUser(request, tenant, email);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtService.generateAccessToken(userPrincipal);

        return new AuthResponse(accessToken, UserResponse.fromEntity(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String slug = StringUtils.hasText(request.getTenantSlug())
                ? request.getTenantSlug().trim().toLowerCase()
                : null;

        User user = findUserForLogin(email, slug);

        validateUserAndTenantActive(user);
        validatePassword(request.getPassword(), user.getPassword());

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtService.generateAccessToken(userPrincipal);

        return new AuthResponse(accessToken, UserResponse.fromEntity(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new BadCredentialsException("No authenticated user in context");
        }
        User user = userRepository.findByIdWithTenant(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userPrincipal.getId()));
        return UserResponse.fromEntity(user);
    }

    // ── Helper methods ──────────────────────────────────────────────────────

    private void validateTenantNotExists(String slug, String name) {
        if (tenantRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Tenant slug '" + slug + "' is already in use");
        }
        if (tenantRepository.existsByName(name)) {
            throw new DuplicateResourceException("Tenant name '" + name + "' is already registered");
        }
    }

    private Tenant createAndSaveTenant(String name, String slug) {
        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setSlug(slug);
        tenant.setStatus(TenantStatus.ACTIVE);
        return tenantRepository.save(tenant);
    }

    private void validateEmailNotInTenant(String email, Long tenantId) {
        if (userRepository.existsByEmailAndTenantId(email, tenantId)) {
            throw new DuplicateResourceException("User with email '" + email + "' already exists in this tenant");
        }
    }

    private User createAndSaveAdminUser(RegisterTenantRequest request, Tenant tenant, String email) {
        User user = new User();
        user.setName(request.getUserName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.TENANT_ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setTenant(tenant);
        return userRepository.save(user);
    }

    private User findUserForLogin(String email, String slug) {
        if (slug != null) {
            return userRepository.findByEmailAndTenantSlug(email, slug)
                    .orElseThrow(() -> new BadCredentialsException("Invalid email, password, or tenant slug"));
        }

        List<User> users = userRepository.findAllByEmailWithTenant(email);
        if (users.isEmpty()) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (users.size() > 1) {
            throw new BadRequestException("Multiple accounts found for this email. Please specify your tenant slug.");
        }
        return users.get(0);
    }

    private void validateUserAndTenantActive(User user) {
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadCredentialsException("Account is inactive. Please contact your administrator.");
        }
        if (user.getTenant() != null && user.getTenant().getStatus() == TenantStatus.INACTIVE) {
            throw new BadCredentialsException("Tenant organization is inactive. Please contact support.");
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}
