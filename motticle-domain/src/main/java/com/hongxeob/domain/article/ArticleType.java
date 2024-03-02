package com.hongxeob.domain.article;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum ArticleType {
	LINK, IMAGE, TEXT;

	public static ArticleType from(String type) {
		validateString(type);

		return Arrays.stream(ArticleType.values())
			.filter(ArticleType -> ArticleType.name().equalsIgnoreCase(type))
			.findFirst()
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ARTICLE_TYPE : {}", type);
				return new BusinessException(ErrorCode.INVALID_ARTICLE_TYPE);
			});
	}

	public static List<ArticleType> from(List<String> type) {
		if (type == null || type.isEmpty()) {
			return Collections.emptyList();
		}
		List<ArticleType> articleTypes =
			type.stream()
				.map(ArticleType::from)
				.toList();
		return articleTypes;
	}

	private static void validateString(String type) {
		if (type == null) {
			throw new BusinessException(ErrorCode.INVALID_ARTICLE_TYPE);
		}
	}
}
