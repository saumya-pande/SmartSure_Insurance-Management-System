package com.dev.authservice.security;

import com.dev.authservice.entity.User;
import com.dev.authservice.repository.RevokedTokenRepository;
import com.dev.authservice.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            try {
                // Check if the token is expired
                if (jwtUtil.isTokenExpired(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid or expired token.");
                    return;
                }
                
                // Check if the token has been revoked
                if (revokedTokenRepository.findById(token).isPresent()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token has been revoked.");
                    return;
                }
                
                // Get the username and role from the token
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                Optional<User> user = userRepository.findByEmail(username);

                if (user.isPresent()) {
                    // Create an authority using the role directly from the enum
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
                    
                    // Create an Authentication object and set it in the SecurityContext
                    SecurityContextHolder.getContext()
                            .setAuthentication(new UsernamePasswordAuthenticationToken(username, null, authorities));
                    
                }
            } catch (Exception ex) {
                // Ignore malformed token if hitting public endpoints like /api/auth
                // but we clear the context just to be safe
                SecurityContextHolder.clearContext();
            }
        }
        
        // Continue the filter chain
        chain.doFilter(request, response);
    }
}
