package com.hongxeob.motticle.article.domain;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryCustom {

	Optional<Article> findByMemberIdAndId(Long memberId, Long id);

	Slice<Article> findAllByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
