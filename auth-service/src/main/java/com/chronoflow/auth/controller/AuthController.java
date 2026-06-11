package com.chronoflow.auth.controller;

import com.chronoflow.auth.service.AuthService;
import com.chronoflow.common.dto.ApiKeyResponse;
import com.chronoflow.common.dto.CreateApiKeyRequest;
import com.chronoflow.common.dto.CreateTenantRequest;
import com.chronoflow.common.dto.TenantResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/tenants")
    public TenantResponse createTenant(@Valid @RequestBody CreateTenantRequest request) {
        return authService.createTenant(request);
    }

    @GetMapping("/tenants")
    public List<TenantResponse> listTenants() {
        return authService.listTenants();
    }

    @PostMapping("/keys")
    public ApiKeyResponse createApiKey(@Valid @RequestBody CreateApiKeyRequest request) {
        return authService.createApiKey(request);
    }
}
