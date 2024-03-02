package com.hongxeob.image.exception;

import com.hongxeob.domain.enumeration.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageIOException extends RuntimeException {

	private final ErrorCode errorCode;
}
