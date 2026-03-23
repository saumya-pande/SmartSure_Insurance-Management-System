package com.dev.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleCheckGatewayFilterFactory
        extends AbstractGatewayFilterFactory<RoleCheckGatewayFilterFactory.Config> {

    public RoleCheckGatewayFilterFactory() {
        super(Config.class);
    }

    public static class Config {
        private String requiredRole;
        public String getRequiredRole() { return requiredRole; }
        public void setRequiredRole(String requiredRole) { this.requiredRole = requiredRole; }
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("requiredRole");  // maps RoleCheck=ROLE_ADMIN to requiredRole field
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // This header was set by AuthenticationGatewayFilterFactory after JWT validation
            String userRole = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-User-Role");

            if (userRole == null || !userRole.equals(config.getRequiredRole())) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }
}