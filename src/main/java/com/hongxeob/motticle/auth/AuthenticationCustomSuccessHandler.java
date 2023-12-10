package com.hongxeob.motticle.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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

	private final JwtUtil jwtUtil;
	private final MemberRepository memberRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

		MyOAuth2member oAuth2User = (MyOAuth2member) authentication.getPrincipal();
		String email = oAuth2User.getAttribute("email");
		String provider = oAuth2User.getAttribute("provider");

		String targetUrl = buildTargetUrl(email, provider, oAuth2User);

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	private String buildTargetUrl(String email, String provider, MyOAuth2member oAuth2User) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("http://localhost:8080/loginSuccess");

		Optional<Member> optionalMember = memberRepository.findById(oAuth2User.getMemberId());

		if (optionalMember.isPresent()) {
			// 회원이 존재할 경우, accessToken을 생성하고 쿼리스트링에 추가
			Member member = optionalMember.get();
			GeneratedToken token = jwtUtil.generatedToken(member.getEmail(), getRole(oAuth2User));
			builder.queryParam("accessToken", token.accessToken());
		} else {
			// 회원이 존재하지 않을 경우, email과 provider를 쿼리스트링에 추가
			builder.queryParam("email", email)
				.queryParam("provider", provider);
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
