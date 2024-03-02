package com.hongxeob.scrap.dto.res;

import com.hongxeob.article.dto.res.ArticleInfoRes;
import com.hongxeob.domain.scrap.Scrap;

public record ScrapRes(
	Long memberId,
	ArticleInfoRes articleInfoRes
) {

	public static ScrapRes from(Scrap scrap) {
		return new ScrapRes(scrap.getMember().getId(), ArticleInfoRes.from(scrap.getArticle()));
	}
}
