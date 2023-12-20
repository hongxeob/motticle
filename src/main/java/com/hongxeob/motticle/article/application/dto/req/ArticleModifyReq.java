package com.hongxeob.motticle.article.application.dto.req;

import com.hongxeob.motticle.article.domain.Article;

public record ArticleModifyReq(
	String title,
	String content,
	String memo,
	boolean isPublic
) {

	public static Article toArticle(ArticleModifyReq req) {
		return Article.builder()
			.title(req.title)
			.content(req.content())
			.memo(req.memo)
			.isPublic(req.isPublic)
			.build();
	}
}
