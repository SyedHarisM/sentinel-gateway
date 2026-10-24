package com.example.sentinel_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SentinelGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SentinelGatewayApplication.class, args);
	}

}


