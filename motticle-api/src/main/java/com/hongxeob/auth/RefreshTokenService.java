package com.hongxeob.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.auth.dto.TokenResponse;
import com.hongxeob.auth.token.JwtUtil;
import com.hongxeob.domain.auth.RefreshToken;
import com.hongxeob.domain.auth.RefreshTokenRepository;
import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtUtil jwtUtil;

	public void removeRefreshToken(String accessToken) {
		RefreshToken token = checkAndGetRefreshToken(accessToken);

		refreshTokenRepository.delete(token);
	}

	public TokenResponse reissueToken(String accessToken) {
		RefreshToken token = checkAndGetRefreshToken(accessToken);
		String role = jwtUtil.getRole(token.getRefreshToken());
		String newAccessToken = jwtUtil.generateAccessToken(token.getId(), role);

		token.updateAccessToken(newAccessToken);
		refreshTokenRepository.save(token);

		return TokenResponse.from(newAccessToken);
	}

	private RefreshToken checkAndGetRefreshToken(String accessToken) {
		RefreshToken token = refreshTokenRepository.findByAccessToken(accessToken)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_REFRESH_TOKEN_BY_ACCESS_TOKEN : {}", accessToken);
				return new EntityNotFoundException(ErrorCode.INVALID_TOKEN);
			});

		return token;
	}
}
