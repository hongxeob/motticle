package com.hongxeob.motticle.article_tag.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.motticle.article_tag.domain.ArticleTag;
import com.hongxeob.motticle.article_tag.domain.ArticleTagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ArticleTagService {

	private final ArticleTagRepository articleTagRepository;

	public ArticleTag save(ArticleTag articleTag) {
		ArticleTag savedArticleTag = articleTagRepository.save(articleTag);

		return savedArticleTag;
	}

}
