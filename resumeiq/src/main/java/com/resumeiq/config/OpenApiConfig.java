package com.resumeiq.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI resumeIqOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ResumeIQ API")
                        .description("AI-powered resume analysis — rule-based and LLM-driven features")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ResumeIQ Team")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Obtain a token via POST /api/v1/auth/login")));
    }
}