package com.hongxeob.motticle.article.application.dto.res;

import java.util.List;

import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.tag.application.dto.res.TagsRes;
import com.hongxeob.motticle.tag.domain.Tag;

public record ArticleInfoRes(
	Long id,
	String title,
	String type,
	String content,
	String memo,
	TagsRes tagsRes,
	boolean isPublic,
	Long memberId
) {

	public static ArticleInfoRes of(Article article, List<Tag> tags) {
		return new ArticleInfoRes(
			article.getId(), article.getTitle(), article.getType().toString()
			, article.getContent(), article.getMemo(), TagsRes.from(tags), article.isPublic(), article.getMember().getId()
		);
	}
}
