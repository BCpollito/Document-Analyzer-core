package com.portfolio.documentanalyzer.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login",
            "/api/v1/users/register"
    );

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Document Analyzer Core API")
                        .version("1.0.0")
                        .description("API REST para análisis y consulta de documentos con RAG y gestión de usuarios.")
                        .contact(new Contact().name("Document Analyzer Team")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT obtenido en el login (sin el prefijo 'Bearer ')")));
    }

    @Bean
    public OpenApiCustomizer securityOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) -> {
                    if (PUBLIC_PATHS.contains(path)) {
                        // Endpoints públicos: sin requisitos de seguridad (no piden autorización)
                        pathItem.readOperations().forEach(operation -> operation.setSecurity(Collections.emptyList()));
                    } else {
                        // Endpoints protegidos: requieren autenticación Bearer JWT
                        pathItem.readOperations().forEach(operation -> {
                            if (operation.getSecurity() == null || operation.getSecurity().isEmpty()) {
                                operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
                            }
                        });
                    }
                });
            }
        };
    }
}
