package com.hongxeob.motticle.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.global.error.exception.EntityNotFoundException;
import com.hongxeob.motticle.global.error.exception.RequiredAuthenticationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult());

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
		ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode());

		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
		ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode());

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(RequiredAuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleRequiredAuthenticationException(RequiredAuthenticationException e) {
		// 인증을 필요로하지만 토큰이 헤더에 들어있지 않은 경우(토큰이 null인 경우)
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.REQUIRED_AUTHENTICATION);

		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
		MissingServletRequestParameterException e) {
		// 쿼리 파라미터에 아무 값도 들어오지 않은 경우
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.REQUIRE_QUERY_PARAM);

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
