package com.example.multitenant.controller;

import com.example.multitenant.dto.auth.TenantResponse;
import com.example.multitenant.dto.response.ApiResponse;
import com.example.multitenant.dto.tenant.UpdateTenantRequest;
import com.example.multitenant.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping({ "/api/tenant/me", "/api/tenants/current", "/api/tenants/me" })
    public ResponseEntity<ApiResponse<TenantResponse>> getCurrentTenant() {
        TenantResponse response = tenantService.getCurrentTenantProfile();
        return ResponseEntity.ok(ApiResponse.success("Current tenant fetched successfully", response));
    }

    @PutMapping("/api/tenant/me")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> updateCurrentTenant(
            @Valid @RequestBody UpdateTenantRequest request) {
        TenantResponse response = tenantService.updateCurrentTenantProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Tenant profile updated successfully", response));
    }
}
