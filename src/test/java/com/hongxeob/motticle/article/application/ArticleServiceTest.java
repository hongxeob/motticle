package com.hongxeob.motticle.article.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.global.util.BucketUtils;
import com.hongxeob.motticle.image.application.ImageService;
import com.hongxeob.motticle.image.application.dto.FileDto;
import com.hongxeob.motticle.image.application.dto.req.ImageUploadReq;
import com.hongxeob.motticle.member.application.MemberService;
import com.hongxeob.motticle.member.domain.GenderType;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.Role;
import com.hongxeob.motticle.tag.application.TagService;
import com.hongxeob.motticle.tag.domain.Tag;


@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

	@Mock
	private ArticleRepository articleRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private ImageService imageService;

	@Mock
	ArticleTagRepository articleTagRepository;

	@Mock
	private TagService tagService;

	@Mock
	private ArticleTagService articleTagService;

	@Mock
	private BucketUtils bucketUtils;

	@InjectMocks
	private ArticleService articleService;

	private Member member;
	private Member member2;
	private Article article;
	private Article article2;
	private Tag tag;
	private ArticleTag articleTag;

	@BeforeEach
	void setUp() {
		member = Member.builder()
			.id(1L)
			.email("sjun2918@naver.com")
			.nickname("호빵")
			.role(Role.GUEST)
			.genderType(null)
			.build();

		member2 = Member.builder()
			.id(2L)
			.email("sjun29128@naver.com")
			.nickname("호빵2")
			.role(Role.GUEST)
			.genderType(GenderType.MALE)
			.build();

		tag = Tag.builder()
			.id(9L)
			.name("미디어")
			.member(member)
			.build();

		article = Article.builder()
			.id(1L)
			.title("제목")
			.type(ArticleType.TEXT)
			.content("내용")
			.memo("메모")
			.isPublic(true)
			.member(member)
			.build();

		article2 = Article.builder()
			.id(2L)
			.title("제목")
			.type(ArticleType.IMAGE)
			.content("내용")
			.memo("메모")
			.isPublic(true)
			.member(member)
			.build();

		articleTag = ArticleTag.builder()
			.article(article)
			.tag(tag)
			.build();
	}

	@Test
	@DisplayName("아티클 등록 성공 - 글 or 링크")
	void registerSuccessTest() throws Exception {

		//given
		List<Long> tagIds = List.of(9L);
		ArticleAddReq articleAddReq = new ArticleAddReq("IT 관련", ArticleType.TEXT.name(), "좋은 글이다.",
			"나중에 볼것", true, tagIds);

		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(articleRepository.save(any()))
			.thenAnswer(invocation -> invocation.getArgument(0));
		when(tagService.getTag(9L))
			.thenReturn(tag);
		when(articleTagService.save(any()))
			.thenAnswer(invocation -> invocation.getArgument(0));

		//when
		ArticleInfoRes articleInfoRes = articleService.register(member.getId(), articleAddReq, null);

		//then
		assertThat(articleInfoRes.title()).isEqualTo(articleAddReq.title());
		assertThat(articleInfoRes.tagsRes().tagRes().get(0).name()).isEqualTo(tag.getName());
	}

	@Test
	@DisplayName("아티클 등록 성공 - 이미지")
	void registerImageSuccessTest() throws Exception {

		// given
		List<Long> tagIds = List.of(9L);
		MultipartFile mockImage = mock(MultipartFile.class);
		ImageUploadReq imageUploadReq = new ImageUploadReq(List.of(mockImage));
		ArticleAddReq articleAddReq = new ArticleAddReq(
			"IT 관련", ArticleType.IMAGE.name(), "좋은 이미지!", "나중에 볼 것", true, tagIds);
		FileDto fileDto = FileDto.builder()
			.originalFileName("originalName")
			.uploadFileName("uploadFileName")
			.uploadFilePath("uploadFilePath")
			.uploadFileUrl("uploadFileUrl")
			.build();

		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(articleRepository.save(any()))
			.thenAnswer(invocation -> invocation.getArgument(0));
		when(tagService.getTag(9L))
			.thenReturn(tag); // Mocking tag service
		when(articleTagService.save(any()))
			.thenAnswer(invocation -> invocation.getArgument(0));

		when(imageService.uploadFiles(any()))
			.thenReturn(List.of(fileDto));

		// when
		ArticleInfoRes articleInfoRes = articleService.register(member.getId(), articleAddReq, imageUploadReq);

		//then
		assertThat(articleInfoRes.content()).isEqualTo(fileDto.getUploadFileUrl());
	}

	@Test
	@DisplayName("아티클 수정 성공 - 텍스트/링크")
	void modifySuccessTest() throws Exception {

		//given
		ArticleModifyReq articleModifyReq = new ArticleModifyReq("수정 제목", "수정 내용", "수정 메모", true);

		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(articleRepository.findById(article.getId()))
			.thenReturn(Optional.of(article));

		//when
		ArticleInfoRes res = articleService.modify(member.getId(), article.getId(), articleModifyReq);

		//then
		assertThat(article.getMemo()).isEqualTo(articleModifyReq.memo());
		assertThat(res.id()).isEqualTo(article.getId());
	}

	@Test
	@DisplayName("아티클에 태그 연결 성공")
	void tagArticleSuccessTest() throws Exception {

		//given
		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(articleRepository.findById(article.getId()))
			.thenReturn(Optional.of(article));
		when(tagService.getTag(tag.getId()))
			.thenReturn(tag);
		when(articleTagRepository.findByArticleIdAndTagId(article.getId(), tag.getId()))
			.thenReturn(Optional.empty());
		when(articleTagRepository.save(any(ArticleTag.class)))
			.thenReturn(articleTag);

		//when
		ArticleInfoRes articleInfoRes = articleService.tagArticle(member.getId(), article.getId(), tag.getId());

		//then
		assertThat(articleInfoRes.id()).isEqualTo(article.getId());
	}

	@Test
	@DisplayName("아티클에 태그 등록 실패 - 이미 존재하는 태그")
	void tagArticleFailTest_alreadyRegisteredTag() throws Exception {

		//given
		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(articleRepository.findById(article.getId()))
			.thenReturn(Optional.of(article));
		when(tagService.getTag(tag.getId()))
			.thenReturn(tag);
		when(articleTagRepository.findByArticleIdAndTagId(article.getId(), tag.getId()))
			.thenReturn(Optional.of(articleTag));

		//when -> then
		assertThrows(BusinessException.class,
			() -> articleService.tagArticle(member.getId(), article.getId(), tag.getId()));
	}

	@Test
	@DisplayName("아티클에 태그 해제 성공")
	void unTagArticleSuccessTest() throws Exception {

		//given
		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(articleRepository.findById(article.getId()))
			.thenReturn(Optional.of(article));
		when(tagService.getTag(tag.getId()))
			.thenReturn(tag);
		when(articleTagRepository.findByArticleIdAndTagId(article.getId(), tag.getId()))
			.thenReturn(Optional.of(articleTag));

		//when -> then
		assertDoesNotThrow(() -> articleService.unTagArticle(member.getId(), article.getId(), tag.getId()));
	}

	@Test
	@DisplayName("아티클에 태그 해제 실패 - 해당 아티클에 태그 존재X")
	void unTagArticleFailTest_notFoundArticleTag() throws Exception {

		//given
		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(articleRepository.findById(article.getId()))
			.thenReturn(Optional.of(article));
		when(tagService.getTag(tag.getId()))
			.thenReturn(tag);
		when(articleTagRepository.findByArticleIdAndTagId(article.getId(), tag.getId()))
			.thenReturn(Optional.empty());

		//when -> then
		assertThrows(BusinessException.class,
			() -> articleService.unTagArticle(member.getId(), article.getId(), tag.getId()));
	}

	@Test
	@DisplayName("해당 아티클로 등록된 모든 태그 해제")
	void unTagByArticleSuccessTest() throws Exception {

		//given
		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(articleRepository.findById(article.getId()))
			.thenReturn(Optional.of(article));

		//when -> then
		assertDoesNotThrow(() -> articleService.unTagArticleByArticle(article.getId(), member.getId()));
	}

	@Test
	@DisplayName("해당 태그로 등록된 모든 아티클에서 해제")
	void unTagByTagSuccessTest() throws Exception {

		//given
		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(tagService.getTag(tag.getId()))
			.thenReturn(tag);

		//when -> then
		assertDoesNotThrow(() -> articleService.unTagArticleByTag(tag.getId(), member.getId()));
	}
}
