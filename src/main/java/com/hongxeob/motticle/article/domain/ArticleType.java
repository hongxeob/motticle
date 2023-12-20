package com.hongxeob.motticle.article.domain;

import java.util.Arrays;

import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.BusinessException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum ArticleType {
	LINK, IMAGE, TEXT;

	public static ArticleType of(String type) {
		validateString(type);

		return Arrays.stream(ArticleType.values())
			.filter(ArticleType -> ArticleType.name().equalsIgnoreCase(type))
			.findFirst()
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ARTICLE_TYPE : {}", type);
				return new BusinessException(ErrorCode.INVALID_ARTICLE_TYPE);
			});
	}

	private static void validateString(String type) {
		if (type == null) {
			throw new BusinessException(ErrorCode.INVALID_ARTICLE_TYPE);
		}
	}
}
