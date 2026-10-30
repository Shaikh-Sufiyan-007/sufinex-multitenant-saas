package com.example.multitenant.dto.dashboard;

import com.example.multitenant.enums.Role;
import com.example.multitenant.enums.UserStatus;

import java.io.Serializable;

public class DashboardStatsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Role role;
    private String tenantName;
    private String tenantSlug;

    // Tenant admin metrics
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;

    // User metrics
    private String userName;
    private String email;
    private UserStatus userStatus;

    public DashboardStatsResponse() {
    }

    // Builder-style factory for TENANT_ADMIN
    public static DashboardStatsResponse forTenantAdmin(String tenantName, String tenantSlug, long totalUsers, long activeUsers, long inactiveUsers) {
        DashboardStatsResponse res = new DashboardStatsResponse();
        res.setRole(Role.TENANT_ADMIN);
        res.setTenantName(tenantName);
        res.setTenantSlug(tenantSlug);
        res.setTotalUsers(totalUsers);
        res.setActiveUsers(activeUsers);
        res.setInactiveUsers(inactiveUsers);
        return res;
    }

    // Builder-style factory for USER
    public static DashboardStatsResponse forUser(String userName, String email, String tenantName, String tenantSlug, UserStatus status) {
        DashboardStatsResponse res = new DashboardStatsResponse();
        res.setRole(Role.USER);
        res.setUserName(userName);
        res.setEmail(email);
        res.setTenantName(tenantName);
        res.setTenantSlug(tenantSlug);
        res.setUserStatus(status);
        return res;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantSlug() {
        return tenantSlug;
    }

    public void setTenantSlug(String tenantSlug) {
        this.tenantSlug = tenantSlug;
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Long getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(Long inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}
