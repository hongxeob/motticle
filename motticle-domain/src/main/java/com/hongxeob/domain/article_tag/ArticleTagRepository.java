package com.hongxeob.domain.article_tag;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {

	Optional<ArticleTag> findByArticleIdAndTagId(Long articleId, Long tagId);

	void deleteAllByArticleId(Long articleId);

	void deleteAllByTagId(Long tagId);
}
