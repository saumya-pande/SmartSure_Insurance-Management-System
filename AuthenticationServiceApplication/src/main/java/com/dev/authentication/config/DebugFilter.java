package com.dev.authentication.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DebugFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {

        System.out.println("=== DEBUG FILTER ===");
        System.out.println("Path: " + request.getRequestURI());
        System.out.println("Method: " + request.getMethod());
        System.out.println("X-User-Email: " + request.getHeader("X-User-Email"));
        System.out.println("X-User-Role: " + request.getHeader("X-User-Role"));

        filterChain.doFilter(request, response);

        System.out.println("=== RESPONSE STATUS: " + response.getStatus() + " ===");
    }
}