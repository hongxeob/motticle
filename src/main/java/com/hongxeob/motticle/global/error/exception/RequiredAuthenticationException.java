package com.hongxeob.motticle.global.error.exception;

import com.hongxeob.motticle.global.error.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequiredAuthenticationException extends RuntimeException {

	private final ErrorCode errorCode;
}
