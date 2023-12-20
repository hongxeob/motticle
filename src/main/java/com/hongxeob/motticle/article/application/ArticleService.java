package com.hongxeob.motticle.article.application;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hongxeob.motticle.article.application.dto.req.ArticleAddReq;
import com.hongxeob.motticle.article.application.dto.req.ArticleModifyReq;
import com.hongxeob.motticle.article.application.dto.res.ArticleInfoRes;
import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.article.domain.ArticleRepository;
import com.hongxeob.motticle.article.domain.ArticleType;
import com.hongxeob.motticle.article_tag.application.ArticleTagService;
import com.hongxeob.motticle.article_tag.domain.ArticleTag;
import com.hongxeob.motticle.article_tag.domain.ArticleTagRepository;
import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.global.error.exception.EntityNotFoundException;
import com.hongxeob.motticle.image.application.ImageService;
import com.hongxeob.motticle.image.application.dto.req.ImageUploadReq;
import com.hongxeob.motticle.image.application.dto.res.ImagesRes;
import com.hongxeob.motticle.member.application.MemberService;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.tag.application.TagService;
import com.hongxeob.motticle.tag.domain.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ArticleService {

	private final ArticleRepository articleRepository;
	private final MemberService memberService;
	private final ImageService imageService;
	private final ArticleTagService articleTagService;
	private final TagService tagService;
	private final ArticleTagRepository articleTagRepository;

	public ArticleInfoRes register(Long memberId, ArticleAddReq req) throws IOException {
		Member member = memberService.getMember(memberId);
		Article article = ArticleAddReq.toArticle(req);

		article.writeBy(member);

		if (article.getType() == ArticleType.IMAGE) {
			validateImageFile(req.file());
			fileUpload(article, List.of(req.file()));
		}

		Article savedArticle = articleRepository.save(article);

		List<Tag> tags = connectionArticleTag(req.tagIds(), article);

		return ArticleInfoRes.of(savedArticle, tags);
	}

	public Long modify(Long memberId, Long articleId, ArticleModifyReq req) throws IOException {
		Member member = memberService.getMember(memberId);

		Article article = getArticle(articleId);

		article.checkArticleOwnerWithRequesterId(member.getId());

		Article modifiedArticle = ArticleModifyReq.toArticle(req);

		article.updateInfo(modifiedArticle);

		return article.getId();
	}

	public ImagesRes uploadImage(Long id, Long memberId, ImageUploadReq req) throws IOException {
		Member member = memberService.getMember(memberId);
		Article article = getArticle(id);
		article.checkArticleOwnerWithRequesterId(member.getId());

		if (article.getType() != ArticleType.IMAGE) {
			throw new BusinessException(ErrorCode.ARTICLE_IS_NOT_IMAGE_TYPE);
		}

		List<String> fileNames = imageService.add(req.file());

		return ImagesRes.from(fileNames);
	}

	// TODO: 12/19/23 동시성 고민(isolation = Isolation.SERIALIZABLE)
	public Long tagArticle(Long memberId, Long id, Long tagId) {
		Member member = memberService.getMember(memberId);

		Article article = getArticle(id);
		article.checkArticleOwnerWithRequesterId(member.getId());

		Tag tag = tagService.getTag(tagId);
		tag.checkTagOwnerWithRequesterId(member.getId());

		articleTagRepository.findByArticleIdAndTagId(article.getId(), tag.getId())
			.ifPresent(articleTag -> {
				log.warn("GET:READ:ALREADY_REGISTERED_BY_TAG_IN_ARTICLE : articleId => {}, tagId: {} ", article.getId(), tag.getId());
				throw new BusinessException(ErrorCode.ALREADY_REGISTERED_BY_TAG_IN_ARTICLE);
			});

		articleTagRepository.save(
			ArticleTag.builder()
				.article(article)
				.tag(tag)
				.build());

		return article.getId();
	}

	public void unTagArticle(Long memberId, Long id, Long tagId) {
		Member member = memberService.getMember(memberId);

		Article article = getArticle(id);
		article.checkArticleOwnerWithRequesterId(member.getId());

		Tag tag = tagService.getTag(tagId);
		tag.checkTagOwnerWithRequesterId(member.getId());

		ArticleTag articleTag = articleTagRepository.findByArticleIdAndTagId(article.getId(), tag.getId())
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ARTICLE_WITH_TAG : articleId => {}, tagId => {} ", article.getId(), tag.getId());
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_REQUEST_TAG_IN_ARTICLE);
			});

		articleTagRepository.delete(articleTag);
	}

	public void unTagArticleByArticle(Long id, Long memberId) {
		Member member = memberService.getMember(memberId);

		Article article = getArticle(id);
		article.checkArticleOwnerWithRequesterId(member.getId());

		articleTagRepository.deleteAllByArticleId(article.getId());
	}

	public void unTagArticleByTag(Long tagId, Long memberId) {
		Member member = memberService.getMember(memberId);

		Tag tag = tagService.getTag(tagId);
		tag.checkTagOwnerWithRequesterId(member.getId());

		articleTagRepository.deleteAllByTagId(tag.getId());
	}

	private Article getArticle(Long articleId) {
		Article article = articleRepository.findById(articleId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ARTICLE_BY_ID : {}", articleId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_ARTICLE);
			});

		return article;
	}

	private void fileUpload(Article article, List<MultipartFile> images) throws IOException {
		List<String> fileNames = imageService.add(images);
		if (!fileNames.isEmpty()) {
			article.setFilePath(fileNames.get(0));
		}
	}

	private List<Tag> connectionArticleTag(List<Long> tagIds, Article article) {
		if (tagIds != null && !tagIds.isEmpty()) {
			List<Tag> tags = tagIds.stream()
				.map(tagService::getTag)
				.collect(Collectors.toList());

			tags.forEach(tag -> articleTagService.save(ArticleTag.builder()
				.article(article)
				.tag(tag)
				.build()));

			return tags;
		}
		return null;
	}

	private void validateImageFile(MultipartFile file) {
		if (file == null) {
			throw new BusinessException(ErrorCode.UPLOAD_IMAGE_FILE);
		}
	}
}
