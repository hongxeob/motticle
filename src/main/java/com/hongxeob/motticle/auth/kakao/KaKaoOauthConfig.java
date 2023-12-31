package com.hongxeob.motticle.auth.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.kakao")
public record KaKaoOauthConfig(
	String redirectUri,
	String clientId,
	String clientSecret,
	String[] scope
) {
}
