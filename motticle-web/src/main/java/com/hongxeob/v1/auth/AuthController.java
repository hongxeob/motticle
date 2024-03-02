package com.hongxeob.v1.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hongxeob.auth.RefreshTokenService;
import com.hongxeob.auth.dto.TokenResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	public static final String TOKEN_HEADER = "Authorization";
	private final RefreshTokenService refreshTokenService;

	@DeleteMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessToken) {
		refreshTokenService.removeRefreshToken(accessToken);

		return ResponseEntity
			.noContent()
			.build();
	}

	@PatchMapping("/reissue")
	public ResponseEntity<TokenResponse> reissueToken(@RequestHeader(TOKEN_HEADER) String accessToken) {
		TokenResponse reissueToken = refreshTokenService.reissueToken(accessToken);

		return ResponseEntity.ok(reissueToken);
	}
}
