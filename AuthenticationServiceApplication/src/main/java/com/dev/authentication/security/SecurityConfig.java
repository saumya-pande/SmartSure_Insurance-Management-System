package com.dev.authentication.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new Argon2PasswordEncoder(16, 32, 1, 1 << 13, 3);
	}
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    
		
		http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // PUBLIC endpoints
            .requestMatchers(
                "/api/auth/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/v3/api-docs/swagger-config",
                "/webjars/**"
            ).permitAll()
            .requestMatchers("/api/auth/**").permitAll()

            // EVERYTHING ELSE → secured
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

	    
	    return http.build();
	}

}