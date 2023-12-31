package com.hongxeob.motticle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MotticleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MotticleApplication.class, args);
	}

}
