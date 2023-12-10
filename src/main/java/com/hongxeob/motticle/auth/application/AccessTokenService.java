package com.hongxeob.motticle.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.motticle.auth.domain.RefreshToken;
import com.hongxeob.motticle.auth.domain.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AccessTokenService {

	private final RefreshTokenRepository tokenRepository;

	public void saveTokenInfo(String email, String refreshToken, String accessToken) {
		RefreshToken token = RefreshToken.builder()
			.id(email)
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();

		tokenRepository.save(token);
	}
}
