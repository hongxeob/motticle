package com.hongxeob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MotticleWebApplication {
	public static void main(String[] args) {
		SpringApplication.run(MotticleWebApplication.class, args);
	}
}
