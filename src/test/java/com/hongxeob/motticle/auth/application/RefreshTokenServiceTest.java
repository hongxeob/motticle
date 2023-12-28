package com.hongxeob.motticle.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hongxeob.motticle.auth.application.dto.TokenResponse;
import com.hongxeob.motticle.auth.domain.RefreshToken;
import com.hongxeob.motticle.auth.domain.RefreshTokenRepository;
import com.hongxeob.motticle.auth.token.JwtUtil;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private RefreshTokenService refreshTokenService;

	private String accessToken;
	private String refreshToken;
	private RefreshToken refreshTokenEntity;

	@BeforeEach
	void setUp() {
		accessToken = "accessToken";
		refreshToken = "refreshToken";
		refreshTokenEntity = RefreshToken.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	@Test
	@DisplayName("refresh 토큰 제거 성공")
	void removeRefreshTokenSuccessTest() throws Exception {

		//given
		when(refreshTokenRepository.findByAccessToken(accessToken))
			.thenReturn(Optional.of(refreshTokenEntity));

		// when
		refreshTokenService.removeRefreshToken(accessToken);

		// then
		verify(refreshTokenRepository, times(1)).delete(refreshTokenEntity);
	}

	@Test
	@DisplayName("토큰 재발급 성공")
	void reissueTokenSuccessTest() {

		// given
		when(refreshTokenRepository.findByAccessToken(accessToken))
			.thenReturn(Optional.of(refreshTokenEntity));

		String newAccessToken = "newAccessToken";
		String role = "ROLE_USER";

		when(jwtUtil.getRole(refreshToken))
			.thenReturn(role);

		when(jwtUtil.generateAccessToken(refreshTokenEntity.getId(), role))
			.thenReturn(newAccessToken);

		// when
		TokenResponse result = refreshTokenService.reissueToken(accessToken);

		// then
		assertEquals(newAccessToken, result.accessToken());
	}
}
