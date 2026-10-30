package com.example.multitenant.service;

import com.example.multitenant.dto.auth.UserResponse;
import com.example.multitenant.dto.response.PageResponse;
import com.example.multitenant.dto.user.ChangePasswordRequest;
import com.example.multitenant.dto.user.CreateUserRequest;
import com.example.multitenant.dto.user.UpdateProfileRequest;
import com.example.multitenant.dto.user.UpdateUserRequest;
import com.example.multitenant.entity.Tenant;
import com.example.multitenant.entity.User;
import com.example.multitenant.enums.UserStatus;
import com.example.multitenant.exception.BadRequestException;
import com.example.multitenant.exception.DuplicateResourceException;
import com.example.multitenant.exception.ResourceNotFoundException;
import com.example.multitenant.repository.TenantRepository;
import com.example.multitenant.repository.UserRepository;
import com.example.multitenant.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public UserService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getTenantUsers(String search, Pageable pageable) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        String cleanSearch = StringUtils.hasText(search) ? search.trim() : null;

        Page<User> page = userRepository.searchTenantUsers(tenantId, cleanSearch, pageable);
        return PageResponse.fromPage(page, UserResponse::fromEntity);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Tenant tenant = findTenantOrThrow(tenantId);

        String email = request.getEmail().trim().toLowerCase();
        validateEmailNotInTenant(email, tenantId);

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);
        user.setTenant(tenant);

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        User user = findTenantUserOrThrow(userId, tenantId);

        user.setName(request.getName().trim());
        user.setRole(request.getRole());

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Transactional
    public UserResponse updateUserStatus(Long userId, UserStatus status) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        User user = findTenantUserOrThrow(userId, tenantId);

        if (userId.equals(currentUserService.getCurrentUserId())) {
            throw new BadRequestException("You cannot deactivate your own account");
        }

        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        User user = currentUserService.getCurrentUserEntity();
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        User user = currentUserService.getCurrentUserEntity();
        user.setName(request.getName().trim());
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUserService.getCurrentUserEntity();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password cannot be the same as current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ── Helper methods ──────────────────────────────────────────────────────

    private Tenant findTenantOrThrow(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));
    }

    private User findTenantUserOrThrow(Long userId, Long tenantId) {
        return userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found or does not belong to your organization"));
    }

    private void validateEmailNotInTenant(String email, Long tenantId) {
        if (userRepository.existsByEmailAndTenantId(email, tenantId)) {
            throw new DuplicateResourceException("User with email '" + email + "' already exists in this tenant");
        }
    }
}
