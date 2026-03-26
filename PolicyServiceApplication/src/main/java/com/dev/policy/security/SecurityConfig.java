package com.dev.policy.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderAuthFilter headerAuthFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
            	    .accessDeniedHandler((request, response, accessDeniedException) -> {
            	        response.setStatus(HttpStatus.FORBIDDEN.value());
            	        response.setContentType("application/json");
            	        response.getWriter().write("""
            	            {
            	              "status": 403,
            	              "error": "Access Denied",
            	              "message": "You do not have permission to access this resource. Required role is missing or insufficient.",
            	              "path": "%s",
            	              "timestamp": "%s"
            	            }
            	            """.formatted(request.getRequestURI(), java.time.LocalDateTime.now()));
            	    })
            	    .authenticationEntryPoint((request, response, authException) -> {
            	        response.setStatus(HttpStatus.UNAUTHORIZED.value());
            	        response.setContentType("application/json");
            	        response.getWriter().write("""
            	            {
            	              "status": 401,
            	              "error": "Unauthorized",
            	              "message": "Authentication is required. Please provide a valid JWT token.",
            	              "path": "%s",
            	              "timestamp": "%s"
            	            }
            	            """.formatted(request.getRequestURI(), java.time.LocalDateTime.now()));
            	    })
            	);

        return http.build();
    }
}
