package com.dev.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;
    private final org.springframework.data.redis.core.ReactiveStringRedisTemplate redisTemplate;

    private boolean isPublicPath(String path) {
        return path.equals("/api/auth/login") ||
                path.equals("/api/auth/register") ||
                path.equals("/api/auth/validate") ||
                path.contains("/v3/api-docs") ||
                path.contains("/swagger-ui") ||
                path.contains("/swagger-resources") ||
                path.contains("/webjars");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Fix for CORS: Allow all OPTIONS preflight requests
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // Allow unauthenticated access to auth + OpenAPI/Swagger endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "please login to continue", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        return redisTemplate.hasKey("revoked:" + token)
                .flatMap(isRevoked -> {
                    if (Boolean.TRUE.equals(isRevoked) || !jwtUtil.validate(token)) {
                        return onError(exchange, "please login to continue", HttpStatus.UNAUTHORIZED);
                    }

                    // extract data
                    var claims = jwtUtil.extractClaims(token);
                    String email = claims.getSubject();
                    String role = claims.get("role", String.class);

                    // add headers
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Email", email)
                            .header("X-User-Role", "ROLE_" + role)
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        byte[] bytes = err.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    // private boolean isPublicPath(String path) {
    // // Auth service endpoints (login/register/etc)
    // if (path.startsWith("/api/auth/")) {
    // return true;
    // }
    //
    // // Gateway swagger + OpenAPI endpoints
    // if (path.equals("/swagger-ui.html")
    // || path.startsWith("/swagger-ui/")
    // || path.startsWith("/v3/api-docs")
    // || path.startsWith("/webjars/")) {
    // return true;
    // }
    //
    // // Proxied OpenAPI docs for downstream services
    // return path.startsWith("/auth/v3/api-docs")
    // || path.startsWith("/policy/v3/api-docs")
    // || path.startsWith("/claims/v3/api-docs");
    // }
}