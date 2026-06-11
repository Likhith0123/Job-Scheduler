package com.chronoflow.auth.controller;

import com.chronoflow.auth.service.AuthService;
import com.chronoflow.common.dto.ApiKeyValidationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/keys")
public class InternalKeyController {

    private final AuthService authService;

    public InternalKeyController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/validate")
    public ApiKeyValidationResponse validate(@RequestParam String key) {
        return authService.validateApiKey(key);
    }
}
