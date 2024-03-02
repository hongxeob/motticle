package com.hongxeob.auth.handler;

import static com.hongxeob.auth.CookieAuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.hongxeob.auth.CookieAuthorizationRequestRepository;
import com.hongxeob.auth.KaKaoOauthProperties;
import com.hongxeob.auth.MyOAuth2member;
import com.hongxeob.auth.token.GeneratedToken;
import com.hongxeob.auth.token.JwtUtil;
import com.hongxeob.common.util.CookieUtils;
import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.EntityNotFoundException;
import com.hongxeob.domain.member.Member;
import com.hongxeob.domain.member.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationCustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private static final String ACCESS_TOKEN = "accessToken";

	private final KaKaoOauthProperties kaKaoOauthProperties;
	private final CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository;
	private final JwtUtil jwtUtil;
	private final MemberRepository memberRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		Member member = findMember(authentication);
		GeneratedToken token = jwtUtil.generatedToken(response, member.getEmail(), member.getRole().getKey());

		String targetUrl = determineTargetUrl(request, response);

		if (response.isCommitted()) {
			log.debug("Response has already been committed.");
			return;
		}

		getRedirectStrategy().sendRedirect(request, response, getRedirectUrl(member, targetUrl, token));
	}

	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
		Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
			.map(Cookie::getValue);
		clearAuthenticationAttributes(request, response);

		if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
			throw new RuntimeException("redirect URIs are not matched.");
		}

		String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

		return targetUrl;
	}

	private Member findMember(Authentication authentication) {
		MyOAuth2member oAuth2User = (MyOAuth2member) authentication.getPrincipal();
		Member member = memberRepository.findById(oAuth2User.getMemberId()).orElseThrow(() -> {
			log.warn("GET:READ:NOT_FOUND_MEMBER_BY_ID : {}", oAuth2User.getMemberId());
			return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
		});

		return member;
	}

	private String getRedirectUrl(Member member, String targetUrl, GeneratedToken token) {
		if (member.getNickname() != null && member.getGenderType() != null) {
			return UriComponentsBuilder.fromUriString(targetUrl)
				.path("/")
				.queryParam(ACCESS_TOKEN, token.accessToken())
				.build().toUriString();
		} else {
			return UriComponentsBuilder.fromUriString(targetUrl)
				.path("/join")
				.queryParam(ACCESS_TOKEN, token.accessToken())
				.build().toUriString();
		}
	}

	protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
		super.clearAuthenticationAttributes(request);
		cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
	}

	private boolean isAuthorizedRedirectUri(String uri) {
		URI clientRedirectUri = URI.create(uri);
		URI authorizedUri = URI.create(kaKaoOauthProperties.redirectUri());

		if (authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
			&& authorizedUri.getPort() == clientRedirectUri.getPort()) {
			return true;
		}
		return false;
	}
}
