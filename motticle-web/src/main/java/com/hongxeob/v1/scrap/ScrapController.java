package com.hongxeob.v1.scrap;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hongxeob.article.dto.res.ArticlesOgRes;
import com.hongxeob.common.aop.CurrentMemberId;
import com.hongxeob.scrap.ScrapService;
import com.hongxeob.scrap.dto.req.ScrapReq;
import com.hongxeob.scrap.dto.res.ScrapRes;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/scraps")
@RequiredArgsConstructor
public class ScrapController {

	private final ScrapService scrapService;

	@CurrentMemberId
	@PostMapping
	public ResponseEntity<ScrapRes> addScrap(Long memberId, @RequestBody @Validated ScrapReq req) {
		ScrapRes scrapRes = scrapService.scrap(memberId, req);

		return ResponseEntity.ok(scrapRes);
	}

	@CurrentMemberId
	@DeleteMapping("/{articleId}")
	public ResponseEntity<Void> deleteScrap(Long memberId, @PathVariable Long articleId) {
		scrapService.removeScrap(memberId, articleId);

		return ResponseEntity
			.noContent()
			.build();
	}

	@CurrentMemberId
	@GetMapping
	public ResponseEntity<ArticlesOgRes> getScrapedArticles(Long memberId) {
		ArticlesOgRes articlesOgRes = scrapService.getAllScrapedArticle(memberId);

		return ResponseEntity.ok(articlesOgRes);
	}

	@CurrentMemberId
	@GetMapping("/{articleId}")
	public ResponseEntity<Boolean> checkScrapedArticle(Long memberId, @PathVariable Long articleId) {
		boolean isScraped = scrapService.isScrapedArticle(memberId, articleId);

		return ResponseEntity.ok(isScraped);
	}
}
