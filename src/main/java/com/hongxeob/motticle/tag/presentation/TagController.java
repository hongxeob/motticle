package com.hongxeob.motticle.tag.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hongxeob.motticle.article.application.ArticleService;
import com.hongxeob.motticle.global.aop.CurrentMemberId;
import com.hongxeob.motticle.tag.application.TagService;
import com.hongxeob.motticle.tag.application.dto.req.TagReq;
import com.hongxeob.motticle.tag.application.dto.res.TagRes;
import com.hongxeob.motticle.tag.application.dto.res.TagsRes;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

	private final TagService tagService;
	private final ArticleService articleService;

	@PostMapping
	@CurrentMemberId
	public ResponseEntity<TagRes> addTag(Long memberId, @RequestBody TagReq req) {
		TagRes tagRes = tagService.register(memberId, req);

		return ResponseEntity.ok(tagRes);
	}

	@GetMapping
	@CurrentMemberId
	public ResponseEntity<TagsRes> getAllTagByMemberId(Long memberId) {
		TagsRes tagsRes = tagService.findAllByMemberId(memberId);

		return ResponseEntity.ok(tagsRes);
	}

	@DeleteMapping("/{id}")
	@CurrentMemberId
	public ResponseEntity<Void> deleteTag(Long memberId, @PathVariable Long id) {
		tagService.delete(memberId, id);
		articleService.unTagArticleByTag(id, memberId);

		return ResponseEntity
			.noContent()
			.build();
	}
}
