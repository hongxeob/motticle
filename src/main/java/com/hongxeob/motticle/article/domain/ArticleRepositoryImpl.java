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
			expression = addExpression(expression, qArticleTag.tag.id.in(tagIds));
		}
		if (!CollectionUtils.isEmpty(articleTypes)) {
			expression = addExpression(expression, qArticle.type.in(articleTypes));
		}
		if (StringUtils.hasText(keyword)) {
			expression = addExpression(expression, qArticle.title.containsIgnoreCase(keyword))
				.or(qArticle.content.containsIgnoreCase(keyword));
		}

		JPAQuery<Article> query = queryFactory.selectFrom(qArticle)
			.leftJoin(qArticle.articleTags, qArticleTag)
			.where(expression)
			.distinct();

		orderSpecifier(sortType, query);

		// 페이징 적용
		List<Article> result = query
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return checkLastPage(pageable, result);
	}

	@Override
	public Slice<Article> findAllWithTagIdAndArticleTypeAndKeyword(Long memberId, Collection<String> tagNames, Collection<ArticleType> articleTypes, String keyword, String sortType, Pageable pageable) {
		BooleanExpression expression = qArticle.isPublic.isTrue();

		if (memberId != null) {
			expression = expression.and(qArticle.member.id.ne(memberId));
		}
		if (!CollectionUtils.isEmpty(tagNames)) {
			expression = expression.and(qArticleTag.tag.name.in(tagNames));
		}
		if (!CollectionUtils.isEmpty(articleTypes)) {
			expression = addExpression(expression, qArticle.type.in(articleTypes));
		}
		if (StringUtils.hasText(keyword)) {
			expression = addExpression(expression, qArticle.title.containsIgnoreCase(keyword)
				.or(qArticle.content.containsIgnoreCase(keyword)));
		}

		JPAQuery<Article> query = queryFactory.selectFrom(qArticle)
			.leftJoin(qArticle.articleTags, qArticleTag)
			.where(expression)
			.distinct();

		orderSpecifier(sortType, query);

		// 페이징 적용
		List<Article> result = query
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return checkLastPage(pageable, result);
	}

	private void orderSpecifier(String sortType, JPAQuery<Article> query) {
		OrderSpecifier<?> orderSpecifier;
		if ("oldest".equalsIgnoreCase(sortType)) {
			orderSpecifier = qArticle.createdAt.asc();
		} else {
			orderSpecifier = qArticle.createdAt.desc();
		}
		query.orderBy(orderSpecifier);
	}

	private BooleanExpression addExpression(BooleanExpression existing, BooleanExpression additional) {
		if (existing == null) {
			return additional;
		} else {
			return existing.and(additional);
		}
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
