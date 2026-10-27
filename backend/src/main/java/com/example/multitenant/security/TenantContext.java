package com.example.multitenant.security;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TENANT_SLUG = new ThreadLocal<>();

    private TenantContext() {
    }

    public static Long getCurrentTenantId() {
        return CURRENT_TENANT_ID.get();
    }

    public static void setCurrentTenantId(Long tenantId) {
        CURRENT_TENANT_ID.set(tenantId);
    }

    public static String getCurrentTenantSlug() {
        return CURRENT_TENANT_SLUG.get();
    }

    public static void setCurrentTenantSlug(String tenantSlug) {
        CURRENT_TENANT_SLUG.set(tenantSlug);
    }

    public static void clear() {
        CURRENT_TENANT_ID.remove();
        CURRENT_TENANT_SLUG.remove();
    }
}
