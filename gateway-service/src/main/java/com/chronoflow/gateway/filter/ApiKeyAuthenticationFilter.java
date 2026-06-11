package com.chronoflow.gateway.filter;

import com.chronoflow.common.dto.ApiKeyValidationResponse;
import com.chronoflow.gateway.client.AuthServiceClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String TENANT_ID_HEADER = "X-Tenant-Id";
    public static final String RATE_LIMIT_HEADER = "X-Rate-Limit-Per-Minute";
    public static final String API_KEY_HEADER = "X-API-Key";

    private final AuthServiceClient authServiceClient;

    public ApiKeyAuthenticationFilter(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/actuator")
                || path.equals("/health")
                || exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);
        if (apiKey == null || apiKey.isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return authServiceClient.validateApiKey(apiKey)
                .flatMap(validation -> {
                    if (!validation.valid()) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                    ServerWebExchange mutated = exchange.mutate()
                            .request(builder -> builder
                                    .header(TENANT_ID_HEADER, validation.tenantId().toString())
                                    .header(RATE_LIMIT_HEADER, String.valueOf(validation.rateLimitPerMinute())))
                            .build();
                    return chain.filter(mutated);
                });
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
