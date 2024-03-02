package com.hongxeob.image.exception;

import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;

import lombok.Getter;


@Getter
public class InvalidFileTypeException extends BusinessException {

	public InvalidFileTypeException(ErrorCode errorCode) {
		super(errorCode);
	}
}
