package com.hongxeob.motticle.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.kakao")
public record KaKaoOauthProperties(
	String redirectUri,
	String clientId,
	String clientSecret,
	String[] scope
) {
}
