package com.hongxeob.motticle.article.application.dto.res;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.tag.application.dto.res.TagRes;
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
	Long memberId,

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	LocalDateTime createdAt,

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	LocalDateTime updatedAt
) {

	public static ArticleInfoRes of(Article article, List<Tag> tags) {
		return new ArticleInfoRes(
			article.getId(), article.getTitle(), article.getType().toString(),
			article.getContent(), article.getMemo(), TagsRes.from(tags), article.isPublic(), article.getMember().getId(),
			article.getCreatedAt(), article.getUpdatedAt()
		);
	}

	public static ArticleInfoRes from(Article article) {
		List<TagRes> tagResList = Stream.ofNullable(article.getArticleTags())
			.flatMap(java.util.Collection::stream)
			.map(articleTag -> TagRes.from(articleTag.getTag()))
			.collect(Collectors.toList());

		TagsRes tagsRes = TagsRes.fromRes(tagResList);

		return new ArticleInfoRes(
			article.getId(), article.getTitle(), article.getType().toString(),
			article.getContent(), article.getMemo(), tagsRes, article.isPublic(), article.getMember().getId(),
			article.getCreatedAt(), article.getUpdatedAt()
		);
	}
}
