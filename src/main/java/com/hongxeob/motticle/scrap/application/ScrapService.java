package com.hongxeob.motticle.scrap.application;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.motticle.article.application.ArticleService;
import com.hongxeob.motticle.article.application.dto.res.ArticlesOgRes;
import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.article.opengraph.OpenGraphProcessor;
import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.global.util.BucketUtils;
import com.hongxeob.motticle.member.application.MemberService;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.scrap.application.dto.req.ScrapReq;
import com.hongxeob.motticle.scrap.application.dto.res.ScrapRes;
import com.hongxeob.motticle.scrap.domain.Scrap;
import com.hongxeob.motticle.scrap.domain.ScrapRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ScrapService {

	private final ScrapRepository scrapRepository;
	private final MemberService memberService;
	private final ArticleService articleService;
	private final OpenGraphProcessor openGraphProcessor;
	private final BucketUtils bucketUtils;

	public ScrapRes scrap(Long memberId, ScrapReq scrapReq) {
		bucketUtils.checkRequestBucketCount();

		Member member = memberService.getMember(memberId);
		Article article = articleService.getArticle(scrapReq.articleId());

		checkPublicArticle(article);

		scrapRepository.findByMemberIdAndArticleId(member.getId(), article.getId())
			.ifPresent(scrap -> {
				log.warn("GET:READ:ALREADY_SCRAPED_ARTICLE_BY_MEMBER : memberId => {}, articleId => {} ", member.getId(), article.getId());
				throw new BusinessException(ErrorCode.ALREADY_SCRAPED_ARTICLE_BY_MEMBER);
			});
		article.increaseScrapCount();

		Scrap scrap = Scrap.builder()
			.member(member)
			.article(article)
			.build();

		Scrap savedScrap = scrapRepository.save(scrap);

		return ScrapRes.from(savedScrap);
	}

	public void removeScrap(Long memberId, Long articleId) {
		bucketUtils.checkRequestBucketCount();

		Member member = memberService.getMember(memberId);
		Article article = articleService.getArticle(articleId);

		Scrap scrap = scrapRepository.findByMemberIdAndArticleId(member.getId(), article.getId())
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_SCRAPING_ARTICLE : memberId => {}, articleId => {} ", member.getId(), article.getId());
				return new BusinessException(ErrorCode.NOT_FOUND_REQUEST_TAG_IN_ARTICLE);
			});

		article.decreaseScrapCount();

		scrapRepository.delete(scrap);
	}

	public void removeAllScrapsForArticle(Long articleId) {
		List<Scrap> scraps = scrapRepository.findAllByArticleId(articleId);

		scrapRepository.deleteAllInBatch(scraps);
	}

	@Transactional(readOnly = true)
	public ArticlesOgRes getAllScrapedArticle(Long memberId) {
		Member member = memberService.getMember(memberId);

		Slice<Article> articles = scrapRepository.findAllArticlesByMemberId(member.getId());

		ArticlesOgRes articlesOgRes = openGraphProcessor.generateArticlesOgResWithOpenGraph(articles);

		return articlesOgRes;
	}

	@Transactional(readOnly = true)
	public boolean isScrapedArticle(Long memberId, Long articleId) {
		Member member = memberService.getMember(memberId);
		Article article = articleService.getArticle(articleId);

		return scrapRepository.existsByMemberIdAndArticleId(member.getId(), article.getId());
	}

	private void checkPublicArticle(Article article) {
		if (!article.isPublic()) {
			log.warn("GET:READ:ARTICLE_IS_PRIVATE : articleId => {} ", article.getId());
			throw new BusinessException(ErrorCode.ARTICLE_IS_PRIVATE);
		}
	}
}
