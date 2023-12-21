package com.hongxeob.motticle.article.application.dto.req;

import java.util.List;

import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.article.domain.ArticleType;

public record ArticleAddReq(
	String title,
	String type,
	String content,
	String memo,
	boolean isPublic,
	List<Long> tagIds
) {

	public static Article toArticle(ArticleAddReq req) {
		return Article.builder()
			.title(req.title)
			.type(ArticleType.of(req.type))
			.content(req.content())
			.memo(req.memo)
			.isPublic(req.isPublic)
			.build();
	}
}
