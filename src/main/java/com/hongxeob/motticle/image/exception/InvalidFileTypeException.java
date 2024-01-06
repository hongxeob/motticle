package com.hongxeob.motticle.image.exception;

import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.BusinessException;

import lombok.Getter;


@Getter
public class InvalidFileTypeException extends BusinessException {

	public InvalidFileTypeException(ErrorCode errorCode) {
		super(errorCode);
	}
}
