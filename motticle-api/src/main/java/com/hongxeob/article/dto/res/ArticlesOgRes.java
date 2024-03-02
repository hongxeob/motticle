package com.hongxeob.article.dto.res;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.hongxeob.domain.article.Article;

public record ArticlesOgRes(
	List<ArticleOgRes> articleOgResList,
	boolean hasNext
) {

	public static ArticlesOgRes of(List<ArticleOgRes> articleOgResList, Slice<Article> articleSlice) {
		return new ArticlesOgRes(articleOgResList, articleSlice.hasNext());
	}
}
