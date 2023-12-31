package com.hongxeob.motticle.auth;

import static com.hongxeob.motticle.auth.domain.CookieAuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.hongxeob.motticle.auth.domain.CookieAuthorizationRequestRepository;
import com.hongxeob.motticle.auth.domain.CookieUtils;
import com.hongxeob.motticle.auth.kakao.KaKaoOauthConfig;
import com.hongxeob.motticle.auth.token.GeneratedToken;
import com.hongxeob.motticle.auth.token.JwtUtil;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationCustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final static String DEFAULT_PATH = "http://localhost:8080";
	private final KaKaoOauthConfig kaKaoOauthConfig;
	private final CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository;
	private final JwtUtil jwtUtil;
	private final MemberRepository memberRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

		String targetUrl = determineTargetUrl(request, response, authentication);

		if (response.isCommitted()) {
			log.debug("Response has already been committed.");
			return;
		}
		clearAuthenticationAttributes(request, response);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(DEFAULT_PATH);

		Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
			.map(Cookie::getValue);

		if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
			throw new RuntimeException("redirect URIs are not matched.");
		}

		String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

		MyOAuth2member oAuth2User = (MyOAuth2member) authentication.getPrincipal();
		Optional<Member> optionalMember = memberRepository.findById(oAuth2User.getMemberId());

		if (optionalMember.isPresent()) {
			// 회원이 존재할 경우, accessToken을 생성하고 쿼리스트링에 추가
			Member member = optionalMember.get();
			GeneratedToken token = jwtUtil.generatedToken(member.getEmail(), getRole(oAuth2User));
			if (optionalMember.get().getNickname() != null && optionalMember.get().getGenderType() != null) {
				return builder
					.path("/home")
					.queryParam("accessToken", token.accessToken())
					.build()
					.encode(StandardCharsets.UTF_8)
					.toUriString();
			} else {
				return builder
					.path("/joinForm")
					.queryParam("accessToken", token.accessToken())
					.build()
					.encode(StandardCharsets.UTF_8)
					.toUriString();
			}
		} else {
			// 회원이 존재하지 않을 경우, email과 provider를 쿼리스트링에 추가
			return UriComponentsBuilder.fromUriString(targetUrl)
				.queryParam("email", oAuth2User.getEmail())
				.build().toUriString();
		}
	}

	protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
		super.clearAuthenticationAttributes(request);
		cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
	}

	private boolean isAuthorizedRedirectUri(String uri) {
		URI clientRedirectUri = URI.create(uri);
		URI authorizedUri = URI.create(kaKaoOauthConfig.redirectUri());

		if (authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
			&& authorizedUri.getPort() == clientRedirectUri.getPort()) {
			return true;
		}
		return false;
	}

	private String buildTargetUrl(String email, MyOAuth2member oAuth2User) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("http://localhost:8080/loginSuccess");

		Optional<Member> optionalMember = memberRepository.findById(oAuth2User.getMemberId());

		if (optionalMember.isPresent()) {
			// 회원이 존재할 경우, accessToken을 생성하고 쿼리스트링에 추가
			Member member = optionalMember.get();
			GeneratedToken token = jwtUtil.generatedToken(member.getEmail(), getRole(oAuth2User));
			builder.queryParam("accessToken", token.accessToken());
		} else {
			// 회원이 존재하지 않을 경우, email과 provider를 쿼리스트링에 추가
			builder.queryParam("email", email);
		}

		return builder.build().encode(StandardCharsets.UTF_8).toUriString();
	}

	private String getRole(MyOAuth2member oAuth2User) {
		return oAuth2User.getAuthorities().stream()
			.findFirst()
			.orElseThrow(IllegalAccessError::new)
			.getAuthority();
	}
}
