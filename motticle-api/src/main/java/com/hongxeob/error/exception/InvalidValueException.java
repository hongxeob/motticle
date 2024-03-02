package com.hongxeob.error.exception;

import com.hongxeob.domain.enumeration.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvalidValueException extends RuntimeException {

	private final ErrorCode errorCode;
}
