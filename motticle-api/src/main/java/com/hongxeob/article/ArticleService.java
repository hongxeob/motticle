package com.hongxeob.article;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hongxeob.article.dto.req.ArticleAddReq;
import com.hongxeob.article.dto.req.ArticleModifyReq;
import com.hongxeob.article.dto.req.SearchReq;
import com.hongxeob.article.dto.res.ArticleInfoRes;
import com.hongxeob.article.dto.res.ArticleOgRes;
import com.hongxeob.article.dto.res.ArticlesOgRes;
import com.hongxeob.article.dto.res.OpenGraphResponse;
import com.hongxeob.article_tag.ArticleTagService;
import com.hongxeob.common.util.BucketUtils;
import com.hongxeob.domain.article.Article;
import com.hongxeob.domain.article.ArticleRepository;
import com.hongxeob.domain.article.ArticleType;
import com.hongxeob.domain.article_tag.ArticleTag;
import com.hongxeob.domain.article_tag.ArticleTagRepository;
import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.exception.EntityNotFoundException;
import com.hongxeob.domain.member.Member;
import com.hongxeob.domain.tag.Tag;
import com.hongxeob.image.ImageService;
import com.hongxeob.image.dto.FileDto;
import com.hongxeob.image.dto.req.ImageUploadReq;
import com.hongxeob.image.dto.res.ImagesRes;
import com.hongxeob.member.MemberService;
import com.hongxeob.opengraph.OpenGraphProcessor;
import com.hongxeob.tag.TagService;

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
	private final BucketUtils bucketUtils;

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
		bucketUtils.checkRequestBucketCount();
		Article article = checkArticleWriterAndRequester(articleId, memberId);

		Article modifiedArticle = ArticleModifyReq.toArticle(req);
		article.updateInfo(modifiedArticle);

		return ArticleInfoRes.from(article);
	}


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
	public ArticlesOgRes findAllByMemberAndCondition(Long memberId, SearchReq searchReq, String keyword) {
		Member member = memberService.getMember(memberId);

		List<ArticleType> types = ArticleType.from(searchReq.articleTypes());

		PageRequest pageable = getPageRequest(searchReq);

		Slice<Article> articleSliceRes = articleRepository.findByMemberIdWithTagIdAndArticleTypeAndKeyword(
			member.getId(),
			searchReq.tagIds(),
			types,
			keyword,
			searchReq.sortOrder(),
			pageable);

		return openGraphProcessor.generateArticlesOgResWithOpenGraph(articleSliceRes);
	}


	@Transactional(readOnly = true)
	public ArticlesOgRes findAllByCondition(Long memberId, SearchReq searchReq, String keyword) {
		List<ArticleType> types = ArticleType.from(searchReq.articleTypes());

		PageRequest pageable = getPageRequest(searchReq);

		Slice<Article> articlesSliceRes = articleRepository.findAllWithTagIdAndArticleTypeAndKeyword(
			memberId,
			searchReq.tagNames(),
			types,
			keyword,
			searchReq.sortOrder(),
			pageable);

		return openGraphProcessor.generateArticlesOgResWithOpenGraph(articlesSliceRes);
	}

	@Transactional(readOnly = true)
	public ArticlesOgRes findAllByConditionAndNotLogin(SearchReq searchReq, String keyword) {
		List<ArticleType> types = ArticleType.from(searchReq.articleTypes());

		PageRequest pageable = getPageRequest(searchReq);

		Slice<Article> articlesSliceRes = articleRepository.findAllWithTagIdAndArticleTypeAndKeywordWithoutLogin(
			searchReq.tagNames(),
			types,
			keyword,
			searchReq.sortOrder(),
			pageable);

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

	private static PageRequest getPageRequest(SearchReq searchReq) {
		int page = searchReq.page();
		page = Math.max(page - 1, 0);
		PageRequest pageable = PageRequest.of(page, searchReq.size());

		return pageable;
	}
}
