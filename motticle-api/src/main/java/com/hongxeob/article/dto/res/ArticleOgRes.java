package com.hongxeob.article.dto.res;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.hongxeob.domain.article.Article;
import com.hongxeob.member.dto.res.MemberInfoRes;
import com.hongxeob.tag.dto.res.TagRes;
import com.hongxeob.tag.dto.res.TagsRes;

public record ArticleOgRes(
	Long id,
	MemberInfoRes memberInfoRes,
	String title,
	String content,
	String type,
	Long scrapCount,
	String memo,
	TagsRes tagsRes,
	boolean isPublic,
	Long memberId,
	OpenGraphResponse openGraphResponse,

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	LocalDateTime createdDatetime,

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime updatedDatetime
) {

	public static ArticleOgRes of(Article article, OpenGraphResponse openGraphResponse) {
		List<TagRes> tagResList = Stream.ofNullable(article.getArticleTags())
			.flatMap(Collection::stream)
			.map(articleTag -> TagRes.from(articleTag.getTag()))
			.collect(Collectors.toList());

		TagsRes tagsRes = TagsRes.fromRes(tagResList);

		return new ArticleOgRes(
			article.getId(), MemberInfoRes.from(article.getMember()), article.getTitle(), article.getContent(),
			article.getType().name(), article.getScrapCount(), article.getMemo(), tagsRes,
			article.isPublic(), article.getMember().getId(),
			openGraphResponse, article.getCreatedAt(), article.getUpdatedAt());
	}
}
