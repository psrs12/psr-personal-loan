package com.personalloan.offeracceptance.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI offerAcceptanceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Offer Acceptance Service API")
                .description("Declarations, e-signature, and ESignCompleted event")
                .version("v1"));
    }
}
