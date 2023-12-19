package com.hongxeob.motticle.auth.token;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.hongxeob.motticle.auth.application.AccessTokenService;
import com.hongxeob.motticle.auth.application.dto.SecurityMemberDto;
import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.EntityNotFoundException;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.MemberRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

	private final JwtProperties jwtProperties;
	private final AccessTokenService tokenService;
	private final MemberRepository memberRepository;
	private String secretKey;

	private static final String TOKEN_HEADER = "Authorization";
	public static final Long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 1;
	public static final Long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 6;// Refresh 토큰 만료 시간 : 6시간

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

	public void verifyToken(String token) {
		Jwts.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token);
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

	public String resolveAccessToken(HttpServletRequest request) {
		return request.getHeader(TOKEN_HEADER);
	}

	public Authentication getAccessTokenAuthentication(String token) {
		Member member = memberRepository.findByEmail(getUid(token))
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_EMAIL : {}", token);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		SecurityMemberDto memberDto = SecurityMemberDto.from(member);

		return new UsernamePasswordAuthenticationToken(memberDto, token,
			List.of(new SimpleGrantedAuthority(memberDto.role())));
	}
}
