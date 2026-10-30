package com.example.multitenant.service;

import com.example.multitenant.dto.dashboard.DashboardStatsResponse;
import com.example.multitenant.entity.Tenant;
import com.example.multitenant.entity.User;
import com.example.multitenant.enums.Role;
import com.example.multitenant.enums.UserStatus;
import com.example.multitenant.repository.TenantRepository;
import com.example.multitenant.repository.UserRepository;
import com.example.multitenant.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final CurrentUserService currentUserService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public DashboardService(
            CurrentUserService currentUserService,
            TenantRepository tenantRepository,
            UserRepository userRepository) {
        this.currentUserService = currentUserService;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        Role role = currentUserService.getCurrentRole();

        if (role == Role.TENANT_ADMIN) {
            return getAdminDashboardStats();
        }

        return getUserDashboardStats();
    }

    private DashboardStatsResponse getAdminDashboardStats() {
        Long tenantId = currentUserService.requireCurrentTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found with id: " + tenantId));

        long totalUsers = userRepository.countByTenantId(tenantId);
        long activeUsers = userRepository.countByTenantIdAndStatus(tenantId, UserStatus.ACTIVE);
        long inactiveUsers = userRepository.countByTenantIdAndStatus(tenantId, UserStatus.INACTIVE);

        return DashboardStatsResponse.forTenantAdmin(
                tenant.getName(),
                tenant.getSlug(),
                totalUsers,
                activeUsers,
                inactiveUsers);
    }

    private DashboardStatsResponse getUserDashboardStats() {
        User user = currentUserService.getCurrentUserEntity();
        Tenant tenant = user.getTenant();

        String tenantName = tenant != null ? tenant.getName() : "N/A";
        String tenantSlug = tenant != null ? tenant.getSlug() : "N/A";

        return DashboardStatsResponse.forUser(
                user.getName(),
                user.getEmail(),
                tenantName,
                tenantSlug,
                user.getStatus());
    }
}
