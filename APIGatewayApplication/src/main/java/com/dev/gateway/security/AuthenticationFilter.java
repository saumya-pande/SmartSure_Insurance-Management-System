package com.dev.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;


import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;
    
    private boolean isPublicPath(String path) {
        System.out.println("PATH: " + path); // debug

        return path.startsWith("/api/auth") ||
               path.startsWith("/auth") ||   // 🔥 CRITICAL
               path.contains("/v3/api-docs") ||
               path.contains("/swagger-ui") ||
               path.contains("/webjars");
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Allow unauthenticated access to auth + OpenAPI/Swagger endpoints
        if (isPublicPath(path)) {
        	
            return chain.filter(exchange);
        }
        
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validate(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
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
    }

//    private boolean isPublicPath(String path) {
//        // Auth service endpoints (login/register/etc)
//        if (path.startsWith("/api/auth/")) {
//            return true;
//        }
//
//        // Gateway swagger + OpenAPI endpoints
//        if (path.equals("/swagger-ui.html")
//                || path.startsWith("/swagger-ui/")
//                || path.startsWith("/v3/api-docs")
//                || path.startsWith("/webjars/")) {
//            return true;
//        }
//
//        // Proxied OpenAPI docs for downstream services
//        return path.startsWith("/auth/v3/api-docs")
//                || path.startsWith("/policy/v3/api-docs")
//                || path.startsWith("/claims/v3/api-docs");
//    }
}