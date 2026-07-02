package com.personalloan.pricingorchestration.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pricingOrchestrationOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Pricing Orchestration Service API")
                .description("Soft pull, offer pricing, hard pull, and final decision routing")
                .version("v1"));
    }
}
