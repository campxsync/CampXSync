package com.campxsync.gateway;

import logger.logging.AppLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ApiGatewayApplication {

    private static final AppLogger log = AppLogger.getLogger(ApiGatewayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        log.info("CampXSync API Gateway microservice started successfully on port 8080.");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
