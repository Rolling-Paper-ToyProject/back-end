package com.sparklenote.common.config;

import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "Authorization";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .components(components)
                .security(List.of(securityRequirement))
                .info(apiInfo())
                .tags(List.of(          // 태그 순서 지정
                        new Tag().name("1. Roll Controller").description("Roll API"),
                        new Tag().name("2. Paper Controller").description("Paper API"),
                        new Tag().name("3. User Controller").description("User API")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("RollingPaper Swagger")
                .description("RollingPaper REST API")
                .version("1.0.0");
    }
}