package com.hongxeob.motticle.article.application.dto.res;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.hongxeob.motticle.article.domain.Article;

public record ArticlesOgRes(
	List<ArticleOgRes> articleOgResList,
	boolean hasNext
) {

	public static ArticlesOgRes of(List<ArticleOgRes> articleOgResList, Slice<Article> articleSlice) {
		return new ArticlesOgRes(articleOgResList, articleSlice.hasNext());
	}
}
