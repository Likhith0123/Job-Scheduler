package com.chronoflow.auth.service;

import com.chronoflow.auth.entity.ApiKey;
import com.chronoflow.auth.entity.Tenant;
import com.chronoflow.auth.repository.ApiKeyRepository;
import com.chronoflow.auth.repository.TenantRepository;
import com.chronoflow.common.dto.ApiKeyResponse;
import com.chronoflow.common.dto.ApiKeyValidationResponse;
import com.chronoflow.common.dto.CreateApiKeyRequest;
import com.chronoflow.common.dto.CreateTenantRequest;
import com.chronoflow.common.dto.TenantResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthService {

    private final TenantRepository tenantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyHasher apiKeyHasher;

    public AuthService(TenantRepository tenantRepository,
                       ApiKeyRepository apiKeyRepository,
                       ApiKeyHasher apiKeyHasher) {
        this.tenantRepository = tenantRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyHasher = apiKeyHasher;
    }

    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        if (tenantRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant already exists");
        }
        Tenant tenant = tenantRepository.save(new Tenant(request.name(), request.rateLimitPerMinute()));
        return toTenantResponse(tenant);
    }

    @Transactional(readOnly = true)
    public List<TenantResponse> listTenants() {
        return tenantRepository.findAll().stream().map(this::toTenantResponse).toList();
    }

    @Transactional
    public ApiKeyResponse createApiKey(CreateApiKeyRequest request) {
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
        if (!tenant.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant is inactive");
        }

        ApiKeyHasher.GeneratedKey generated = apiKeyHasher.generate();
        String label = request.label() == null || request.label().isBlank() ? "default" : request.label();
        ApiKey apiKey = apiKeyRepository.save(new ApiKey(
                tenant.getId(),
                label,
                generated.keyHash(),
                generated.keyPrefix()
        ));

        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getTenantId(),
                apiKey.getLabel(),
                generated.apiKey(),
                apiKey.isActive(),
                apiKey.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ApiKeyValidationResponse validateApiKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return ApiKeyValidationResponse.invalid();
        }

        return apiKeyRepository.findByKeyHashAndActiveTrue(apiKeyHasher.hash(rawKey))
                .flatMap(apiKey -> tenantRepository.findById(apiKey.getTenantId())
                        .filter(Tenant::isActive)
                        .map(tenant -> new ApiKeyValidationResponse(true, tenant.getId(), tenant.getRateLimitPerMinute())))
                .orElseGet(ApiKeyValidationResponse::invalid);
    }

    private TenantResponse toTenantResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getRateLimitPerMinute(),
                tenant.isActive(),
                tenant.getCreatedAt()
        );
    }
}
