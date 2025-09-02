package com.healthcare.admin_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Admin Service API",
                version = "1.0",
                description = "API documentation for the Admin Service in the Healthcare Microservices Project.",
                license = @License(
                        name = "Apache 2.0",
                        url = "http://springdoc.org"
                )
        )
)
public class OpenApiConfig {
    // No bean definition needed, metadata handled by annotations
}

