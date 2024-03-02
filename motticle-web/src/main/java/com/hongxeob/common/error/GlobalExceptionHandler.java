package com.hongxeob.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.exception.EntityNotFoundException;
import com.hongxeob.error.ErrorResponse;
import com.hongxeob.error.exception.ExcessiveRequestException;
import com.hongxeob.error.exception.RequiredAuthenticationException;

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
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.REQUIRED_AUTHENTICATION);

		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
		MissingServletRequestParameterException e) {
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.REQUIRE_QUERY_PARAM);

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ExcessiveRequestException.class)
	public ResponseEntity<ErrorResponse> handleExcessiveRequestException(ExcessiveRequestException e) {
		ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode());

		return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
	}
}
