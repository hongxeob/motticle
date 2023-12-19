package com.hongxeob.motticle.auth.token.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hongxeob.motticle.auth.application.dto.SecurityMemberDto;
import com.hongxeob.motticle.auth.token.JwtUtil;
import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.EntityNotFoundException;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final MemberRepository memberRepository;

	private static final String TOKEN_HEADER = "Authorization";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String accessToken = request.getHeader(TOKEN_HEADER);

		if (!StringUtils.hasText(accessToken)) {
			doFilter(request, response, filterChain);
			return;
		}

		jwtUtil.verifyToken(accessToken);

		// AccessToken 내부의 payload에 있는 email로 member를 조회한다.
		// 없다면 예외를 발생시킨다 -> 정상 케이스가 아님
		Member member = memberRepository.findByEmail(jwtUtil.getUid(accessToken))
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_EMAIL : {}", accessToken);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		SecurityMemberDto memberDto = SecurityMemberDto.from(member);

		Authentication auth = getAuthentication(memberDto, accessToken);
		SecurityContextHolder.getContext().setAuthentication(auth);
		log.info("세션에 저장된 객체 => {}", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return request.getRequestURI().contains("auth/");
	}

	public Authentication getAuthentication(SecurityMemberDto member, String accessToken) {
		return new UsernamePasswordAuthenticationToken(member, accessToken,
			List.of(new SimpleGrantedAuthority(member.role())));
	}
}
