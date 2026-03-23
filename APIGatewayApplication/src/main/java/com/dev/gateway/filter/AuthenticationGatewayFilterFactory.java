package com.dev.gateway.filter;

import com.dev.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (request.getURI().getPath().contains("/api/auth")) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return this.onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            } else {
                return this.onError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
            }

            try {
                jwtUtil.validateToken(authHeader);

                String username = jwtUtil.extractUsername(authHeader);
                String role = jwtUtil.extractRole(authHeader);
                Long userId = jwtUtil.extractUserId(authHeader);

                // Strip spoofed headers, then set real values from validated JWT
                request = exchange.getRequest()
                        .mutate()
                        .headers(headers -> {
                            headers.remove("X-LoggedIn-User");
                            headers.remove("X-User-Role");
                            headers.remove("X-User-Id");
                        })
                        .header("X-LoggedIn-User", username)
                        .header("X-User-Role", role)
                        .header("X-User-Id", String.valueOf(userId))
                        .build();

            } catch (Exception e) {
                System.out.println("Invalid access...!");
                return this.onError(exchange, "Invalid/Expired Token", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    private Mono<Void> onError(org.springframework.web.server.ServerWebExchange exchange, String err,
            HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
}
