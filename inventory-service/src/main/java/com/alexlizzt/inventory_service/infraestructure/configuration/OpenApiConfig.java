package com.alexlizzt.inventory_service.infraestructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Microservice API")
                        .version("1.0.0")
                        .description("Microservicio de inventario basado en Clean Architecture / Hexagonal con búsqueda semántica soportada por IA (FastAPI).")
                        .contact(new Contact()
                                .name("Alex Lizzt")
                                .email("alex@example.com")));
    }
}
