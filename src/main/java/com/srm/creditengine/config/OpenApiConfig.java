package com.srm.creditengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI creditEngineOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SRM Credit Engine API")
                .version("0.1.0")
                .description("API de precificação e liquidação auditável de recebíveis BRL/USD.")
                .contact(new Contact().name("SRM Credit Engine")));
    }
}
