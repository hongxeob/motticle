package com.hongxeob.motticle.article.domain;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.hongxeob.motticle.article_tag.domain.QArticleTag;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private QArticle qArticle = QArticle.article;
	private QArticleTag qArticleTag = QArticleTag.articleTag;

	@Override
	public Slice<Article> findByMemberIdWithTagIdAndArticleTypeAndKeyword(
		Long memberId,
		Collection<Long> tagIds,
		Collection<ArticleType> articleTypes,
		String keyword,
		String sortType,
		Pageable pageable) {

		BooleanExpression expression = qArticle.member.id.eq(memberId);
		if (!CollectionUtils.isEmpty(tagIds)) {
			expression = expression.and(qArticleTag.tag.id.in(tagIds));
		}
		if (!CollectionUtils.isEmpty(articleTypes)) {
			expression = expression.and(qArticle.type.in(articleTypes));
		}
		if (StringUtils.hasText(keyword)) {
			expression = expression.and(qArticle.title.containsIgnoreCase(keyword)
				.or(qArticle.content.containsIgnoreCase(keyword)));
		}

		JPAQuery<Article> query = queryFactory.selectFrom(qArticle)
			.leftJoin(qArticle.articleTags, qArticleTag)
			.where(expression)
			.distinct();

		OrderSpecifier<?> orderSpecifier;
		if ("oldest".equalsIgnoreCase(sortType)) {
			orderSpecifier = qArticle.createdAt.asc();
		} else {
			orderSpecifier = qArticle.createdAt.desc();
		}
		query.orderBy(orderSpecifier);

		// 페이징 적용
		List<Article> result = query
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return checkLastPage(pageable, result);
	}

	private Slice<Article> checkLastPage(Pageable pageable, List<Article> result) {
		boolean hasNext = false;

		if (result.size() > pageable.getPageSize()) {
			hasNext = true;
			result.remove(pageable.getPageSize());
		}

		return new SliceImpl<>(result, pageable, hasNext);
	}
}