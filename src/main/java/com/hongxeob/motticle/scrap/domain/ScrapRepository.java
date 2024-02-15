package com.hongxeob.motticle.scrap.domain;

import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hongxeob.motticle.article.domain.Article;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

	Optional<Scrap> findByMemberIdAndArticleId(Long memberId, Long articleId);

	@Query("SELECT s.article FROM Scrap s JOIN s.article WHERE s.member.id = :memberId")
	Slice<Article> findAllArticlesByMemberId(@Param("memberId") Long memberId);

	@Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Scrap s WHERE s.member.id = :memberId AND s.article.id = :articleId")
	boolean existsByMemberIdAndArticleId(@Param("memberId") Long memberId, @Param("articleId") Long articleId);
}
