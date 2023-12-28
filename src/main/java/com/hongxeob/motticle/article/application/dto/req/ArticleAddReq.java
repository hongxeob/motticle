package com.hongxeob.motticle.article.application.dto.req;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.article.domain.ArticleType;

public record ArticleAddReq(
	@NotBlank(message = "제목은 필수 입력 입니다.")
	String title,
	@NotBlank(message = "타입은 필수 입력 입니다.")
	String type,
	String content,
	String memo,
	@NotNull(message = "공개 여부를 선택해 주세요.")
	boolean isPublic,
	List<Long> tagIds
) {

	public static Article toArticle(ArticleAddReq req) {
		return Article.builder()
			.title(req.title)
			.type(ArticleType.from(req.type))
			.content(req.content())
			.memo(req.memo)
			.isPublic(req.isPublic)
			.build();
	}
}
