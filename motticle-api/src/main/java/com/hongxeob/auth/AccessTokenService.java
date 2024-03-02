package com.hongxeob.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.domain.auth.RefreshToken;
import com.hongxeob.domain.auth.RefreshTokenRepository;

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
