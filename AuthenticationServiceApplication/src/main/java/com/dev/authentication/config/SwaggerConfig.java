package com.dev.authentication.config;


import io.swagger.v3.oas.models.servers.Server;
import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
	            .info(new Info()
	                .title("SmartSure Insurance Management System API")
	                .description("API Gateway for SmartSure Insurance Microservices")
	                .version("1.0.0")
	            )
	            .servers(List.of(
	                new Server()
	                    .url("http://localhost:8080")
	                    .description("Local Development Server"),
	                new Server()
	                    .url("https://api.smartsure.com")
	                    .description("Production Server")
	            ))
	            .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
	            .components(new Components()
	                .addSecuritySchemes("BearerAuth",
	                    new SecurityScheme()
	                        .type(SecurityScheme.Type.HTTP)
	                        .scheme("bearer")
	                        .bearerFormat("JWT")
	                        .description("Enter JWT token")
	                )
	            );
	    }
}
