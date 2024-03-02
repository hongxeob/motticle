package com.hongxeob.auth.token;

public record GeneratedToken(
	String accessToken,
	String refreshToken,
	Long accessTokenExpiresIn
) {
}
