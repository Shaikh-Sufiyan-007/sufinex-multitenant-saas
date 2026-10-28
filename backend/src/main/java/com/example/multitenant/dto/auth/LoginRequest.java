package com.example.multitenant.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String tenantSlug;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password, String tenantSlug) {
        this.email = email;
        this.password = password;
        this.tenantSlug = tenantSlug;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTenantSlug() {
        return tenantSlug;
    }

    public void setTenantSlug(String tenantSlug) {
        this.tenantSlug = tenantSlug;
    }
}
