package com.hongxeob.auth.token;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix="spring.jwt")
public class JwtProperties {
	private String secret;
}
