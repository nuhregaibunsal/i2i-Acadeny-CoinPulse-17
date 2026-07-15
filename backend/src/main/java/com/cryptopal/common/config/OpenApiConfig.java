package com.cryptopal.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cryptoPalOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("CryptoPal Core API")
                .description("Backend API for the CryptoPal trading & AI-insights platform")
                .version("v1"));
    }
}
