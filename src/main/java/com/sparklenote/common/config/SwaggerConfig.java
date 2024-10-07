package com.sparklenote.common.config;

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
        String jwtSchemeName = "Authorization";  // JWT 인증을 위한 헤더 명칭
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)  // HTTP 타입을 명시
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .components(components)  // JWT 설정을 적용한 컴포넌트를 추가
                .security(List.of(securityRequirement))  // 보안 요구 사항 설정
                .info(apiInfo());  // API 정보 설정
    }

    private Info apiInfo() {
        return new Info()
                .title("RollingPaper Swagger")
                .description("RollingPaper REST API")
                .version("1.0.0");
    }
}
