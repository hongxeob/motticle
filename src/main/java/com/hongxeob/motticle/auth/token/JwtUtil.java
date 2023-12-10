package com.hongxeob.motticle.auth.token;

import java.util.Base64;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.hongxeob.motticle.auth.application.AccessTokenService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtUtil {

	private final JwtProperties jwtProperties;
	private final AccessTokenService tokenService;
	private String secretKey;

	public static final Long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 1;
	public static final Long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 6;// Refresh 토큰 만료 시간 : 6시간
	public static final Long REISSUE_EXPIRE_TIME = 1000L * 60 * 60 * 3; // Reissue 만료 시간 : 3시간

	@PostConstruct
	protected void init() {
		secretKey = Base64.getEncoder().encodeToString(jwtProperties.getSecret().getBytes());
	}

	public GeneratedToken generatedToken(String email, String role) {
		// refreshToken과 accessToken을 생성한다.
		String refreshToken = generateRefreshToken(email, role);
		String accessToken = generateAccessToken(email, role);
		long now = (new Date().getTime());
		Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

		// 토큰을 Redis에 저장한다.
		tokenService.saveTokenInfo(email, refreshToken, accessToken);
		return new GeneratedToken(accessToken, refreshToken, accessTokenExpiresIn.getTime());
	}

	public String generateRefreshToken(String email, String role) {
		// 토큰의 유효 기간을 밀리초 단위로 설정.
		Long refreshPeriod = REFRESH_TOKEN_EXPIRE_TIME;

		// 새로운 클레임 객체를 생성하고, 이메일과 역할(권한)을 셋팅
		Claims claims = Jwts.claims().setSubject(email);
		claims.put("role", role);

		// 현재 시간과 날짜를 가져온다.
		Date now = new Date();

		return Jwts.builder()
			// Payload를 구성하는 속성들을 정의한다.
			.setClaims(claims)
			// 발행일자를 넣는다.
			.setIssuedAt(now)
			// 토큰의 만료일시를 설정한다.
			.setExpiration(new Date(now.getTime() + refreshPeriod))
			// 지정된 서명 알고리즘과 비밀 키를 사용하여 토큰을 서명한다.
			.signWith(SignatureAlgorithm.HS256, secretKey)
			.compact();
	}

	public String generateAccessToken(String email, String role) {
		Long tokenPeriod = ACCESS_TOKEN_EXPIRE_TIME;
		Claims claims = Jwts.claims().setSubject(email);
		claims.put("role", role);

		Date now = new Date();
		return
			Jwts.builder()
				// Payload를 구성하는 속성들을 정의한다.
				.setClaims(claims)
				// 발행일자를 넣는다.
				.setIssuedAt(now)
				// 토큰의 만료일시를 설정한다.
				.setExpiration(new Date(now.getTime() + tokenPeriod))
				// 지정된 서명 알고리즘과 비밀 키를 사용하여 토큰을 서명한다.
				.signWith(SignatureAlgorithm.HS256, secretKey)
				.compact();
	}

	public boolean verifyToken(String token) {
		try {
			Jws<Claims> claims = Jwts.parser()
				.setSigningKey(secretKey) // 비밀키를 설정하여 파싱한다.
				.parseClaimsJws(token);  // 주어진 토큰을 파싱하여 Claims 객체를 얻는다.
			// 토큰의 만료 시간과 현재 시간비교
			return claims.getBody()
				.getExpiration()
				.after(new Date());  // 만료 시간이 현재 시간 이후인지 확인하여 유효성 검사 결과를 반환
		} catch (Exception e) {
			return false;
		}
	}

	// 토큰에서 Email을 추출한다.
	public String getUid(String token) {
		return Jwts.parser()
			.setSigningKey(secretKey)
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}

	// 토큰에서 ROLE(권한)만 추출한다.
	public String getRole(String token) {
		return Jwts.parser()
			.setSigningKey(secretKey)
			.parseClaimsJws(token)
			.getBody()
			.get("role", String.class);
	}
}
