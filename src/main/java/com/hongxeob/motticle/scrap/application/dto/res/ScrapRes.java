package com.hongxeob.motticle.scrap.application.dto.res;

import com.hongxeob.motticle.article.application.dto.res.ArticleInfoRes;
import com.hongxeob.motticle.scrap.domain.Scrap;

public record ScrapRes(
	Long memberId,
	ArticleInfoRes articleInfoRes
) {

	public static ScrapRes from(Scrap scrap) {
		return new ScrapRes(scrap.getId(), ArticleInfoRes.from(scrap.getArticle()));
	}
}
