package com.hongxeob.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hongxeob.auth.CustomOAuth2UserService;
import com.hongxeob.auth.handler.AuthenticationCustomFailureHandler;
import com.hongxeob.auth.handler.AuthenticationCustomSuccessHandler;
import com.hongxeob.common.filter.JwtAuthFilter;
import com.hongxeob.common.filter.JwtExceptionFilter;

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
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authorizeRequests()
			.antMatchers("/api/auth/**").permitAll()
			.antMatchers("/**", "/css/**", "images/**", "/js/**", "/favicon.ico", "/h2-console/**").permitAll()
			.anyRequest().authenticated()
			.and()
			.oauth2Login()
			.loginPage("/kakao")
			.userInfoEndpoint().userService(customOAuth2UserService)
			.and()
			.failureHandler(oAuth2LoginFailureHandler)
			.successHandler(oAuth2LoginSuccessHandler);

		return http
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtExceptionFilter, JwtAuthFilter.class)
			.build();
	}
}
