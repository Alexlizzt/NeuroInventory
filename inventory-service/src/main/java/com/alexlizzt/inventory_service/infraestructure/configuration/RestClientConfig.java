package com.alexlizzt.inventory_service.infraestructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${ai.service.host}")
    private String aiServiceHost;

    @Value("${ai.service.port}")
    private String aiServicePort;

    @Value("${ai.service.api-key}")
    private String internalApiKey;

    @Bean
    public RestClient aiRestClient() {
        // Configurar Timeouts para dar margen a la inferencia del LLM local
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(180)); // 3 minutos de lectura

        String baseUrl = String.format("http://%s:%s/api/v1/rag", aiServiceHost, aiServicePort);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("X-API-KEY", internalApiKey)
                .build();
    }
}