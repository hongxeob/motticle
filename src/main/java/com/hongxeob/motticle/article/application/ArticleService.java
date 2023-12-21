package com.hongxeob.motticle.article.application;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hongxeob.motticle.article.application.dto.req.ArticleAddReq;
import com.hongxeob.motticle.article.application.dto.req.ArticleModifyReq;
import com.hongxeob.motticle.article.application.dto.res.ArticleInfoRes;
import com.hongxeob.motticle.article.application.dto.res.ArticleOgRes;
import com.hongxeob.motticle.article.application.dto.res.OpenGraphResponse;
import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.article.domain.ArticleRepository;
import com.hongxeob.motticle.article.domain.ArticleType;
import com.hongxeob.motticle.article.opengraph.OpenGraphService;
import com.hongxeob.motticle.article.opengraph.OpenGraphVO;
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
	private final OpenGraphService openGraphService;

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

	public Long modify(Long articleId, Long memberId, ArticleModifyReq req) {
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

		ArticleTag articleTag = articleTagRepository.save(
			ArticleTag.builder()
				.article(article)
				.tag(tag)
				.build());

		article.addTag(articleTag);

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

	@Transactional(readOnly = true)
	public ArticleOgRes findArticleByMemberId(Long id, Long memberId) {

		Member member = memberService.getMember(memberId);

		Article article = articleRepository.findByMemberIdAndId(member.getId(), id)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ARTICLE_WITH_MEMBER_ID : articleId => {}, memberId => {} ", id, member.getId());
				return new BusinessException(ErrorCode.NOT_FOUND_ARTICLE);
			});

		article.setFilePath(getFilePath(article.getType(), article.getContent()));
		return ArticleOgRes.of(article, getOpenGraphResponse(article.getType(), article.getContent()));
	}

	public void remove(Long memberId, Long id) {
		Member member = memberService.getMember(memberId);

		Article article = getArticle(id);
		article.checkArticleOwnerWithRequesterId(member.getId());

		articleRepository.delete(article);
	}

	private String getFilePath(ArticleType type, String content) {
		if (type == ArticleType.IMAGE) {
			return imageService.getFilePath(content);
		}
		return content;
	}

	private OpenGraphResponse getOpenGraphResponse(ArticleType articleType, String link) {
		if (articleType != ArticleType.LINK) {
			throw new BusinessException(ErrorCode.LINK_TYPE_ONLY_USE);
		}

		Optional<OpenGraphVO> openGraphVoOptional = openGraphService.getMetadata(link);

		if (openGraphVoOptional.isEmpty()) {
			throw new BusinessException(ErrorCode.LINK_CANNOT_BE_EMPTY);
		}

		OpenGraphVO openGraphVo = openGraphVoOptional.get();

		OpenGraphResponse openGraphResponse = OpenGraphResponse.of(
			HttpStatus.OK.value(),
			openGraphVo.image(),
			openGraphVo.siteName(),
			openGraphVo.title(),
			openGraphVo.url() != null ? openGraphVo.url() : link,
			openGraphVo.description()
		);

		return openGraphResponse;
	}

	private Article getArticle(Long articleId) {
		Article article = articleRepository.findById(articleId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ARTICLE_BY_ID : {}", articleId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_ARTICLE);
			});

		return article;
	}

	private void fileUpload(Article article, ImageUploadReq req) throws IOException {
		List<String> fileNames = imageService.add(req.file());
		if (!fileNames.isEmpty()) {
			article.setFilePath(fileNames.get(0));
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
