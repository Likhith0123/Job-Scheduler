package com.chronoflow.gateway.client;

import com.chronoflow.common.dto.ApiKeyValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(@Value("${chronoflow.auth-service.url}") String authServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(authServiceUrl).build();
    }

    public Mono<ApiKeyValidationResponse> validateApiKey(String apiKey) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/keys/validate")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApiKeyValidationResponse.class)
                .onErrorReturn(ApiKeyValidationResponse.invalid());
    }
}
