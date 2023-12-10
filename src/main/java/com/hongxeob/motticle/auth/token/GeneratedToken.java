package com.hongxeob.motticle.auth.token;

public record GeneratedToken(
	String accessToken,
	String refreshToken,
	Long accessTokenExpiresIn
) {
}
