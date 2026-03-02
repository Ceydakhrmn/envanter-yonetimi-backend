package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Konfigürasyonu
 * API dokümantasyonunu özelleştirir
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Corporate Inventory and User Management System API")
                        .version("1.0.0")
                        .description("""
                                This API provides CRUD operations for corporate user management.
                                
                                **Features:**
                                - Create, update, delete users
                                - Search by email and department
                                - Soft delete (user deactivation)
                                - Input validation
                                """)
                        .contact(new Contact()
                                .name("Efsora Backend Team")
                                .email("support@efsora.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ));
    }
}
