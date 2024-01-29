package com.hongxeob.motticle.article.application;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hongxeob.motticle.article.application.dto.req.ArticleAddReq;
import com.hongxeob.motticle.article.application.dto.req.ArticleModifyReq;
import com.hongxeob.motticle.article.application.dto.res.ArticleInfoRes;
import com.hongxeob.motticle.article.application.dto.res.ArticleOgRes;
import com.hongxeob.motticle.article.application.dto.res.ArticlesOgRes;
import com.hongxeob.motticle.article.application.dto.res.OpenGraphResponse;
import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.article.domain.ArticleRepository;
import com.hongxeob.motticle.article.domain.ArticleType;
import com.hongxeob.motticle.article.opengraph.OpenGraphProcessor;
import com.hongxeob.motticle.article_tag.application.ArticleTagService;
import com.hongxeob.motticle.article_tag.domain.ArticleTag;
import com.hongxeob.motticle.article_tag.domain.ArticleTagRepository;
import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.global.error.exception.EntityNotFoundException;
import com.hongxeob.motticle.image.application.ImageService;
import com.hongxeob.motticle.image.application.dto.FileDto;
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
	private final OpenGraphProcessor openGraphProcessor;

	public ArticleInfoRes register(Long memberId, ArticleAddReq req, ImageUploadReq imageReq) throws IOException {
		Member member = memberService.getMember(memberId);
		Article article = ArticleAddReq.toArticle(req);

		article.writeBy(member);

		if (article.getType() == ArticleType.IMAGE) {
			validateImageFile(imageReq.file());
			fileUpload(article, imageReq);
		}

		Article savedArticle = articleRepository.save(article);

		List<Tag> tags = connectionArticleTag(req.tagIds(), article);

		return ArticleInfoRes.of(savedArticle, tags);
	}

	public ArticleInfoRes modify(Long articleId, Long memberId, ArticleModifyReq req) {
		Article article = checkArticleWriterAndRequester(articleId, memberId);

		Article modifiedArticle = ArticleModifyReq.toArticle(req);
		article.updateInfo(modifiedArticle);

		return ArticleInfoRes.from(article);
	}

	// TODO: 12/29/23 공개 -> 비공개로 될 때 이미 스크랩 당한 아티클들 처리 고민 (프론트 단에서?)
	public void modifyPublicStatus(Long articleId, Long memberId) {
		Article article = checkArticleWriterAndRequester(articleId, memberId);

		article.updatePublicStatus();
	}

	public ImagesRes uploadImage(Long id, Long memberId, ImageUploadReq req) throws IOException {
		Article article = checkArticleWriterAndRequester(id, memberId);

		if (article.getType() != ArticleType.IMAGE) {
			throw new BusinessException(ErrorCode.ARTICLE_IS_NOT_IMAGE_TYPE);
		}

		List<FileDto> fileDtos = imageService.uploadFiles(req.file());

		return ImagesRes.from(fileDtos);
	}

	// TODO: 12/19/23 동시성 고민(isolation = Isolation.SERIALIZABLE)

	public ArticleInfoRes tagArticle(Long memberId, Long id, Long tagId) {
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

		ArticleTag articleTag = articleTagRepository.save(
			ArticleTag.builder()
				.article(article)
				.tag(tag)
				.build());

		article.addTag(articleTag);

		return ArticleInfoRes.from(article);
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
				return new BusinessException(ErrorCode.NOT_FOUND_REQUEST_TAG_IN_ARTICLE);
			});

		article.removeTag(articleTag);

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

	@Transactional(readOnly = true)
	public ArticleOgRes findByMemberId(Long id, Long memberId) {

		Member member = memberService.getMember(memberId);

		Article article = articleRepository.findByMemberIdAndId(member.getId(), id)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ARTICLE_WITH_MEMBER_ID : articleId => {}, memberId => {} ", id, member.getId());
				return new BusinessException(ErrorCode.NOT_FOUND_ARTICLE);
			});

		article.setFilePath(article.getContent());
		OpenGraphResponse openGraphResponse = openGraphProcessor.getOpenGraphResponse(article.getType(), article.getContent());

		return ArticleOgRes.of(article, openGraphResponse);
	}

	@Transactional(readOnly = true)
	public ArticleOgRes findById(Long id) {

		Article article = articleRepository.findById(id)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ARTICLE : articleId => {} ", id);
				return new BusinessException(ErrorCode.NOT_FOUND_ARTICLE);
			});

		OpenGraphResponse openGraphResponse = openGraphProcessor.getOpenGraphResponse(article.getType(), article.getContent());

		return ArticleOgRes.of(article, openGraphResponse);
	}

	@Transactional(readOnly = true)
	public ArticlesOgRes findAllByMemberId(Long memberId, Pageable pageable) {
		Member member = memberService.getMember(memberId);
		Slice<Article> articleList = articleRepository.findAllByMemberIdOrderByCreatedAtDesc(member.getId(), pageable);

		return openGraphProcessor.generateArticlesOgResWithOpenGraph(articleList);
	}

	@Transactional(readOnly = true)
	public ArticlesOgRes findAllByMemberAndCondition(Long memberId, List<Long> tagIds, List<String> articleTypes,
													 String keyword, String sortOrder, Pageable pageable) {
		Member member = memberService.getMember(memberId);

		List<ArticleType> types = ArticleType.from(articleTypes);

		Slice<Article> articleSliceRes = articleRepository.findByMemberIdWithTagIdAndArticleTypeAndKeyword(
			member.getId(), tagIds, types,
			keyword, sortOrder, pageable);

		return openGraphProcessor.generateArticlesOgResWithOpenGraph(articleSliceRes);
	}

	@Transactional(readOnly = true)
	public ArticlesOgRes findAllByCondition(Long memberId, List<String> tagNames, List<String> articleTypes,
											String keyword, String sortOrder, Pageable pageable) {
		List<ArticleType> types = ArticleType.from(articleTypes);

		Slice<Article> articlesSliceRes = articleRepository.findAllWithTagIdAndArticleTypeAndKeyword(memberId, tagNames, types, keyword, sortOrder, pageable);

		return openGraphProcessor.generateArticlesOgResWithOpenGraph(articlesSliceRes);
	}

	public void remove(Long memberId, Long id) {
		Member member = memberService.getMember(memberId);

		Article article = getArticle(id);
		article.checkArticleOwnerWithRequesterId(member.getId());

		articleRepository.delete(article);
	}

	public OpenGraphResponse getOpenGraphResponse(ArticleType articleType, String link) {
		OpenGraphResponse openGraphResponse = openGraphProcessor.getOpenGraphResponse(articleType, link);

		return openGraphResponse;
	}

	private Article checkArticleWriterAndRequester(Long articleId, Long memberId) {
		Member member = memberService.getMember(memberId);

		Article article = getArticle(articleId);
		article.checkArticleOwnerWithRequesterId(member.getId());
		return article;
	}

	@Transactional(readOnly = true)
	public Article getArticle(Long articleId) {
		Article article = articleRepository.findById(articleId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ARTICLE_BY_ID : {}", articleId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_ARTICLE);
			});

		return article;
	}

	private void fileUpload(Article article, ImageUploadReq req) throws IOException {
		List<FileDto> fileDtos = imageService.uploadFiles(req.file());

		if (!fileDtos.get(0).getUploadFileName().isEmpty()) {
			article.setFilePath(fileDtos.get(0).getUploadFileUrl());
		}
	}

	private List<Tag> connectionArticleTag(List<Long> tagIds, Article article) {
		if (tagIds != null && !tagIds.isEmpty()) {
			List<Tag> tags = tagIds.stream()
				.map(tagService::getTag)
				.collect(Collectors.toList());

			tags.forEach(tag -> {
				ArticleTag articleTag = ArticleTag.builder()
					.article(article)
					.tag(tag)
					.build();
				articleTagService.save(articleTag);
				article.addTag(articleTag);
			});

			return tags;
		}

		return null;
	}

	private void validateImageFile(List<MultipartFile> files) {
		if (files == null) {
			throw new BusinessException(ErrorCode.UPLOAD_IMAGE_FILE);
		}
	}
}
