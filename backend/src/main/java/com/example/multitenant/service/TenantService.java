package com.example.multitenant.service;

import com.example.multitenant.dto.auth.TenantResponse;
import com.example.multitenant.dto.tenant.UpdateTenantRequest;
import com.example.multitenant.entity.Tenant;
import com.example.multitenant.exception.DuplicateResourceException;
import com.example.multitenant.exception.ResourceNotFoundException;
import com.example.multitenant.repository.TenantRepository;
import com.example.multitenant.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final CurrentUserService currentUserService;

    public TenantService(TenantRepository tenantRepository, CurrentUserService currentUserService) {
        this.tenantRepository = tenantRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public Tenant findBySlug(String slug) {
        return tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with slug: " + slug));
    }

    @Transactional(readOnly = true)
    public Tenant findById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return tenantRepository.existsBySlug(slug);
    }

    @Transactional(readOnly = true)
    public TenantResponse getCurrentTenantProfile() {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Tenant tenant = findById(tenantId);
        return TenantResponse.fromEntity(tenant);
    }

    @Transactional
    public TenantResponse updateCurrentTenantProfile(UpdateTenantRequest request) {
        Long tenantId = currentUserService.requireCurrentTenantId();
        Tenant tenant = findById(tenantId);

        String newName = request.getName().trim();
        validateTenantNameAvailable(tenant, newName);

        tenant.setName(newName);
        Tenant updated = tenantRepository.save(tenant);
        return TenantResponse.fromEntity(updated);
    }

    private void validateTenantNameAvailable(Tenant currentTenant, String newName) {
        if (!currentTenant.getName().equalsIgnoreCase(newName) && tenantRepository.existsByName(newName)) {
            throw new DuplicateResourceException("Tenant name '" + newName + "' is already in use");
        }
    }
}
