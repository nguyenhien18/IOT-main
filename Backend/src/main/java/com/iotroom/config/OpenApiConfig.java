package com.iotroom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {
    public static final String JWT_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI iotRoomOpenApi() {
        SecurityScheme jwtScheme = new SecurityScheme()
                .name(JWT_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Nhap JWT nhan duoc tu POST /api/auth/login. Khong can them tien to Bearer.");

        return new OpenAPI()
                .info(new Info()
                        .title("IoT Room Monitoring API")
                        .version("1.0.0")
                        .description("API giam sat cam bien va dieu khien thiet bi phong IoT qua MQTT.")
                        .contact(new Contact().name("IoT Room Monitoring")))
                .components(new Components().addSecuritySchemes(JWT_SCHEME, jwtScheme))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME));
    }
}
