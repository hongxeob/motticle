package com.hongxeob.v1.article;

import java.io.IOException;
import java.util.List;

import javax.validation.constraints.NotBlank;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hongxeob.article.ArticleService;
import com.hongxeob.article.dto.req.ArticleAddReq;
import com.hongxeob.article.dto.req.ArticleModifyReq;
import com.hongxeob.article.dto.req.ArticleTaqReq;
import com.hongxeob.article.dto.req.SearchReq;
import com.hongxeob.article.dto.res.ArticleInfoRes;
import com.hongxeob.article.dto.res.ArticleOgRes;
import com.hongxeob.article.dto.res.ArticlesOgRes;
import com.hongxeob.article.dto.res.OpenGraphResponse;
import com.hongxeob.common.aop.CurrentMemberId;
import com.hongxeob.domain.article.ArticleType;
import com.hongxeob.image.dto.req.ImageUploadReq;
import com.hongxeob.image.dto.res.ImagesRes;
import com.hongxeob.scrap.ScrapService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

	private final ArticleService articleService;
	private final ScrapService scrapService;
	private static final String DEFAULT_PAGING_SIZE = "10";

	@CurrentMemberId
	@PostMapping
	public ResponseEntity<ArticleInfoRes> addArticle(Long memberId,
													 @RequestPart @Validated ArticleAddReq articleAddReq,
													 @RequestPart(required = false) List<MultipartFile> image) throws IOException {
		ImageUploadReq imageUploadReq = new ImageUploadReq(image);

		ArticleInfoRes articleInfoRes = articleService.register(memberId, articleAddReq, imageUploadReq);

		return ResponseEntity.ok(articleInfoRes);
	}

	@CurrentMemberId
	@GetMapping("{id}")
	public ResponseEntity<ArticleOgRes> getArticleByMemberId(@PathVariable Long id, Long memberId) {
		ArticleOgRes articleOgRes = articleService.findByMemberId(id, memberId);

		return ResponseEntity.ok(articleOgRes);
	}

	@GetMapping("{articleId}/details")
	public ResponseEntity<ArticleOgRes> getArticleById(@PathVariable Long articleId) {
		ArticleOgRes articleOgRes = articleService.findById(articleId);

		return ResponseEntity.ok(articleOgRes);
	}

	@CurrentMemberId
	@GetMapping
	public ResponseEntity<ArticlesOgRes> getArticlesByMemberId(Long memberId,
															   @RequestParam(required = false, defaultValue = "0") int page,
															   @RequestParam(required = false, defaultValue = DEFAULT_PAGING_SIZE) int size) {
		page = Math.max(page - 1, 0);
		PageRequest pageable = PageRequest.of(page, size);
		ArticlesOgRes articleResponse = articleService.findAllByMemberId(memberId, pageable);

		return ResponseEntity.ok(articleResponse);
	}

	@CurrentMemberId
	@GetMapping("/search")
	public ResponseEntity<ArticlesOgRes> getArticlesByMemberAndCondition(Long memberId,
																		 @Validated SearchReq searchReq,
																		 @RequestParam(required = false) String keyword
	) {
		ArticlesOgRes articleRes = articleService.findAllByMemberAndCondition(memberId, searchReq, keyword);

		return ResponseEntity.ok(articleRes);
	}

	@CurrentMemberId
	@GetMapping("/explore")
	public ResponseEntity<ArticlesOgRes> getArticlesByCondition(Long memberId,
																@Validated SearchReq searchReq,
																@RequestParam(required = false) String keyword
	) {
		ArticlesOgRes articleRes = articleService.findAllByCondition(memberId, searchReq, keyword);

		return ResponseEntity.ok(articleRes);
	}

	@GetMapping("/welcome")
	public ResponseEntity<ArticlesOgRes> getArticlesByConditionWithoutLogin(@Validated SearchReq searchReq,
																			@RequestParam(required = false) String keyword
	) {
		ArticlesOgRes articleRes = articleService.findAllByConditionAndNotLogin(searchReq, keyword);

		return ResponseEntity.ok(articleRes);
	}

	@CurrentMemberId
	@PatchMapping("{id}")
	public ResponseEntity<ArticleInfoRes> updateArticle(@PathVariable Long id, Long memberId, @RequestBody ArticleModifyReq req) {
		ArticleInfoRes articleInfoRes = articleService.modify(id, memberId, req);

		return ResponseEntity.ok(articleInfoRes);
	}

	@CurrentMemberId
	@PatchMapping("/status/{id}")
	public ResponseEntity<Void> updateArticlePublicStatus(@PathVariable Long id, Long memberId) {
		articleService.modifyPublicStatus(id, memberId);

		return ResponseEntity
			.noContent()
			.build();
	}

	@CurrentMemberId
	@PostMapping("/images/{id}")
	public ResponseEntity<ImagesRes> uploadImages(@PathVariable Long id, Long memberId,
												  @RequestPart(required = false) List<MultipartFile> image) throws IOException {
		ImageUploadReq imageUploadReq = new ImageUploadReq(image);

		ImagesRes imagesRes = articleService.uploadImage(id, memberId, imageUploadReq);

		return ResponseEntity.ok(imagesRes);
	}

	@CurrentMemberId
	@PostMapping("/{id}/tags")
	public ResponseEntity<ArticleInfoRes> addTagToArticle(@PathVariable Long id, @RequestBody @Validated ArticleTaqReq articleTaqReq, Long memberId) {
		ArticleInfoRes articleInfoRes = articleService.tagArticle(memberId, id, articleTaqReq.tagId());

		return ResponseEntity.ok(articleInfoRes);
	}

	@CurrentMemberId
	@DeleteMapping("/{id}/un-tags")
	public ResponseEntity<Void> removeTagFromArticle(@PathVariable Long id, @RequestParam(name = "tag") Long tagId, Long memberId) {
		articleService.unTagArticle(memberId, id, tagId);

		return ResponseEntity
			.noContent().
			build();
	}

	@CurrentMemberId
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteArticle(@PathVariable Long id, Long memberId) {
		articleService.unTagArticleByArticle(id, memberId);
		scrapService.removeAllScrapsForArticle(id);
		articleService.remove(memberId, id);

		return ResponseEntity.noContent()
			.build();
	}

	@GetMapping("/validation")
	public ResponseEntity<OpenGraphResponse> validationLink(@RequestParam @NotBlank String link) {
		OpenGraphResponse openGraphResponse = articleService.getOpenGraphResponse(ArticleType.LINK, link);

		return ResponseEntity.ok(openGraphResponse);
	}
}
