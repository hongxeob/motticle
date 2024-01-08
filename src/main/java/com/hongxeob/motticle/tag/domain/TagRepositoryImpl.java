package com.hongxeob.motticle.tag.domain;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;
	private QTag qTag = QTag.tag;

	@Override
	public Slice<Tag> findAllByMemberId(Long memberId, Pageable pageable) {
		JPAQuery<Tag> query = jpaQueryFactory.selectFrom(qTag)
			.where(qTag.member.id.eq(memberId));

		List<Tag> result = query
			.limit(pageable.getPageSize() + 1)
			.orderBy(qTag.createdAt.desc())
			.fetch();

		return checkLastPage(pageable, result);
	}

	private Slice<Tag> checkLastPage(Pageable pageable, List<Tag> result) {
		boolean hasNext = false;

		if (result.size() > pageable.getPageSize()) {
			hasNext = true;
			result.remove(pageable.getPageSize());
		}

		return new SliceImpl<>(result, pageable, hasNext);
	}
}
