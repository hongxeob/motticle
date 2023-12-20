package com.hongxeob.motticle.image.exception;

import com.hongxeob.motticle.global.error.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageIOException extends RuntimeException {

	private final ErrorCode errorCode;
}
