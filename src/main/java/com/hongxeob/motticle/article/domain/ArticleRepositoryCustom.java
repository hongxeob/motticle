package com.hongxeob.motticle.article.domain;

import java.util.Collection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ArticleRepositoryCustom {

	Slice<Article> findByMemberIdWithTagIdAndArticleTypeAndKeyword(
		Long memberId,
		Collection<Long> tagIds,
		Collection<ArticleType> articleTypes,
		String keyword,
		String sortType,
		Pageable pageable
	);

	Slice<Article> findAllWithTagIdAndArticleTypeAndKeyword(
		Long memberId,
		Collection<String> tagNames,
		Collection<ArticleType> articleTypes,
		String keyword,
		String sortType,
		Pageable pageable
	);
}
