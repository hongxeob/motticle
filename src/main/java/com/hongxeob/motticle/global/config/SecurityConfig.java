package com.hongxeob.motticle.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hongxeob.motticle.auth.AuthenticationCustomFailureHandler;
import com.hongxeob.motticle.auth.AuthenticationCustomSuccessHandler;
import com.hongxeob.motticle.auth.application.CustomOAuth2UserService;
import com.hongxeob.motticle.auth.token.filter.JwtAuthFilter;
import com.hongxeob.motticle.auth.token.filter.JwtExceptionFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final AuthenticationCustomSuccessHandler oAuth2LoginSuccessHandler;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final JwtAuthFilter jwtAuthFilter;
	private final AuthenticationCustomFailureHandler oAuth2LoginFailureHandler;
	private final JwtExceptionFilter jwtExceptionFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.httpBasic().disable()
			.cors().and()
			.csrf().disable()
			.formLogin().disable()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션관리 정책을 STATELESS(세션이 있으면 쓰지도 않고, 없으면 만들지도 않는다)
			.and()
			.authorizeRequests() // 요청에 대한 인증 설정
			.antMatchers("/api/auth/**").permitAll() // 토큰 발급 관련 경로 모두 허용
			.antMatchers("/**", "/css/**", "images/**", "/js/**", "/favicon.ico", "/h2-console/**").permitAll()
			.anyRequest().authenticated()
			.and()
			.oauth2Login()
			.userInfoEndpoint().userService(customOAuth2UserService) // OAuth2 로그인시 사용자 정보를 가져오는 엔드포인트와 사용자 서비스를 설정
			.and()
			.failureHandler(oAuth2LoginFailureHandler)
			.successHandler(oAuth2LoginSuccessHandler);

		// JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가한다.
		return http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtExceptionFilter, JwtAuthFilter.class)
			.build();
	}
}
