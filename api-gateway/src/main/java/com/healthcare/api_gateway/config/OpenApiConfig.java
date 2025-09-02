package com.healthcare.api_gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Healthcare API Gateway")
                        .description("API Gateway for Healthcare Microservices. This gateway aggregates multiple service APIs into a single Swagger UI.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Healthcare Dev Team")
                                .email("support@healthcare.com")
                                .url("https://healthcare.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}

