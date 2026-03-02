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
                        .title("Kurumsal Envanter ve Kullanıcı Yönetim Sistemi API")
                        .version("1.0.0")
                        .description("""
                                Bu API kurumsal kullanıcı yönetimi için CRUD işlemlerini sağlar.
                                
                                **Özellikler:**
                                - Kullanıcı oluşturma, güncelleme, silme
                                - Email ve departmana göre arama
                                - Soft delete (kullanıcı pasifleştirme)
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
