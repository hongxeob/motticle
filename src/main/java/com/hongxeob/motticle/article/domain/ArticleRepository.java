package com.hongxeob.motticle.article.domain;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

	Optional<Article> findByMemberIdAndId(Long memberId, Long id);

	Slice<Article> findAllByMemberId(Long memberId, Pageable pageable);
}
