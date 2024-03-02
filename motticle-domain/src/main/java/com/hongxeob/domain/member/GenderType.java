package com.hongxeob.domain.member;

import java.util.Arrays;

import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum GenderType {
	MALE, FEMALE;

	public static GenderType of(String gender) {
		validateString(gender);

		return Arrays.stream(GenderType.values())
			.filter(genderType -> genderType.name().equalsIgnoreCase(gender))
			.findFirst()
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_GENDER_TYPE : {}", gender);
				return new BusinessException(ErrorCode.INVALID_GENDER_TYPE);
			});
	}

	private static void validateString(String gender) {
		if (gender == null) {
			throw new BusinessException(ErrorCode.GENDER_CANNOT_BE_EMPTY);
		}
	}
}
