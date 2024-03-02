package com.hongxeob.domain.exception;

import com.hongxeob.domain.enumeration.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EntityNotFoundException extends RuntimeException {

	private final ErrorCode errorCode;
}
