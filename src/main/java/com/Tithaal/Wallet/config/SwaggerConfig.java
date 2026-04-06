package com.Tithaal.Wallet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Wallet Application API")
                                                .version("1.0")
                                                .description("API documentation for the Wallet Application"))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme()
                                                                                .name("bearerAuth")
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")));
        }

        @Bean
        public GroupedOpenApi publicApi() {
                return GroupedOpenApi.builder()
                                .group("users")
                                .pathsToMatch("/api/wallet/**", "/api/ledger/**", "/api/users/**")
                                .pathsToExclude("/api/organizations/**")
                                .build();
        }

        @Bean
        public GroupedOpenApi adminApi() {
                return GroupedOpenApi.builder()
                                .group("admin")
                                .pathsToMatch("/api/organizations/**")
                                .pathsToExclude("/api/auth/register/**")
                                .build();
        }

        @Bean
        public GroupedOpenApi platformApi() {
                return GroupedOpenApi.builder()
                                .group("super_admin")
                                .pathsToMatch("/api/platform/**")
                                .build();
        }
}
