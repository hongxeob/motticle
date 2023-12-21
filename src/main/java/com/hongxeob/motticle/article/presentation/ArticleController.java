package com.hongxeob.motticle.article.presentation;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hongxeob.motticle.article.application.ArticleService;
import com.hongxeob.motticle.article.application.dto.req.ArticleAddReq;
import com.hongxeob.motticle.article.application.dto.req.ArticleModifyReq;
import com.hongxeob.motticle.article.application.dto.res.ArticleInfoRes;
import com.hongxeob.motticle.global.aop.CurrentMemberId;
import com.hongxeob.motticle.image.application.dto.req.ImageUploadReq;
import com.hongxeob.motticle.image.application.dto.res.ImagesRes;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

	private final ArticleService articleService;

	@CurrentMemberId
	@PostMapping
	public ResponseEntity<ArticleInfoRes> register(Long memberId, @RequestPart @Validated ArticleAddReq req, @RequestPart(required = false) List<MultipartFile> image) throws IOException {
		ImageUploadReq imageUploadReq = new ImageUploadReq(image);

		ArticleInfoRes articleInfoRes = articleService.register(memberId, req, imageUploadReq);

		return ResponseEntity.ok(articleInfoRes);
	}

	@CurrentMemberId
	@PatchMapping("{id}")
	public ResponseEntity<Long> modify(@PathVariable Long id, Long memberId, @RequestBody ArticleModifyReq req) {
		Long modifiedArticleId = articleService.modify(memberId, id, req);

		return ResponseEntity.ok(modifiedArticleId);
	}

	@CurrentMemberId
	@PostMapping("/images/{id}")
	public ResponseEntity<ImagesRes> uploadImages(@PathVariable Long id, Long memberId, @RequestPart(required = false) List<MultipartFile> image) throws IOException {
		ImageUploadReq imageUploadReq = new ImageUploadReq(image);

		ImagesRes imagesRes = articleService.uploadImage(id, memberId, imageUploadReq);

		return ResponseEntity.ok(imagesRes);
	}

	@CurrentMemberId
	@PostMapping("/{id}/tags")
	public ResponseEntity<Long> addTagToArticle(@PathVariable Long id, @RequestParam(name = "tag") Long tagId, Long memberId) {
		Long taggedArticleId = articleService.tagArticle(memberId, id, tagId);

		return ResponseEntity.ok(taggedArticleId);
	}

	@CurrentMemberId
	@DeleteMapping("/{id}/un-tags/{tagId}")
	public ResponseEntity<Void> removeTagFromArticle(@PathVariable Long id, @PathVariable Long tagId, Long memberId) {
		articleService.unTagArticle(memberId, id, tagId);

		return ResponseEntity
			.noContent().
			build();
	}

	@CurrentMemberId
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> remove(@PathVariable Long id, Long memberId) {
		articleService.unTagArticleByArticle(id, memberId);
		articleService.remove(memberId, id);

		return ResponseEntity.noContent()
			.build();
	}
}
