package com.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("PI-System API")
                                                .version("1.1.0")
                                                .description("Portfolio Intelligence System (Fintech Backend) API Documentation.\n\n"
                                                                +
                                                                "### Authentication\n" +
                                                                "Use the 'Authorize' button below. Enter your JWT token in the format: `Bearer <token>`.\n"
                                                                +
                                                                "Registration tokens are automatically generated, or use the /login endpoint.")
                                                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                                .addTagsItem(new Tag().name("Auth")
                                                .description("Authentication & registration management"))
                                .addTagsItem(new Tag().name("Portfolio")
                                                .description("High-level portfolio aggregation & analytics"))
                                .addTagsItem(new Tag().name("Holdings")
                                                .description("Detailed equity, mutual fund, and ETF data"))
                                .addTagsItem(new Tag().name("Admin")
                                                .description("Internal management and feature controls"))
                                .addTagsItem(new Tag().name("Wealth")
                                                .description("Savings, FD, RD, and Loan management"))
                                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                                .components(new Components()
                                                .addSecuritySchemes("bearer-jwt",
                                                                new SecurityScheme()
                                                                                .name("Authorization")
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("Enter JWT token only. Example: `eyJhbG...`")));
        }
}
