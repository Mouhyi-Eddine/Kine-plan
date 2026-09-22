package com.kineplan.shared.infrastructure.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI kinePlanOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Kine-plan API")
                .version("v1")
                .description("API de gestion multi-cabinets pour cabinets de kinesitherapie"));
    }
}