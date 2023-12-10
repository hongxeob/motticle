package com.hongxeob.motticle.auth.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hongxeob.motticle.auth.application.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	public static final String TOKEN_HEADER = "Authorization";
	private final RefreshTokenService refreshTokenService;

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessToken) {
		refreshTokenService.removeRefreshToken(accessToken);

		return ResponseEntity
			.noContent()
			.build();
	}

	// TODO: 12/10/23 응답값 객체로 바꿀 것
	@PatchMapping("/reissue")
	public ResponseEntity<String> reissueToken(@RequestHeader(TOKEN_HEADER) String accessToken) {
		String reissuedToken = refreshTokenService.reissueToken(accessToken);

		return ResponseEntity.ok(reissuedToken);
	}
}
