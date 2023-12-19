package com.hongxeob.motticle.auth.token.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.ErrorResponse;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

	private static final String TOKEN_HEADER = "Authorization";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (ExpiredJwtException e) {
			String accessToken = request.getHeader(TOKEN_HEADER);
			log.error("만료된 토큰. 토큰 => {} 만료일 => {}", accessToken, e.getClaims().getExpiration());
			// 토큰이 만료된 경우 401 Unauthorized를 보낸다.
			writeErrorResponse(response, ErrorCode.EXPIRED_TOKEN, HttpServletResponse.SC_UNAUTHORIZED);
			return;
		} catch (RuntimeException e) {
			String accessToken = request.getHeader(TOKEN_HEADER);
			log.error("올바르지 않은 토큰의 유효성(형식, 서명 등) => {}", accessToken);
			// 토큰이 올바르지 않은 경우 401 Unauthorized를 보낸다.
			writeErrorResponse(response, ErrorCode.INVALID_TOKEN, HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
	}

	private void writeToHttpServletResponse(HttpServletResponse response, int statusCode, String errorMessage) throws
		IOException {
		response.setStatus(statusCode);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(errorMessage);
		response.getWriter().flush();
		response.getWriter().close();
	}

	private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode, int statusCode) throws
		IOException {
		String errorResponseJsonFormat = getErrorResponseJsonFormat(errorCode);
		writeToHttpServletResponse(response, statusCode, errorResponseJsonFormat);
	}

	private String getErrorResponseJsonFormat(ErrorCode errorCode) throws JsonProcessingException {
		ErrorResponse errorResponse = ErrorResponse.of(errorCode);
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(errorResponse);
	}
}
