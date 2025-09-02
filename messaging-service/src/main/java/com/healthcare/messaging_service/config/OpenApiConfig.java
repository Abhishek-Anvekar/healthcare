package com.healthcare.messaging_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Messaging Service API", version = "1.0", description = "Send SMS/OTP and consume events"),
        servers = {@Server(url = "http://localhost:8090", description = "Local")}
)
public class OpenApiConfig {}
