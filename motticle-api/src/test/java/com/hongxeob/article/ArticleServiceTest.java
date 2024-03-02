package com.hongxeob.article;

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

import com.hongxeob.article.dto.req.ArticleAddReq;
import com.hongxeob.article.dto.req.ArticleModifyReq;
import com.hongxeob.article.dto.res.ArticleInfoRes;
import com.hongxeob.article_tag.ArticleTagService;
import com.hongxeob.common.util.BucketUtils;
import com.hongxeob.domain.article.Article;
import com.hongxeob.domain.article.ArticleRepository;
import com.hongxeob.domain.article.ArticleType;
import com.hongxeob.domain.article_tag.ArticleTag;
import com.hongxeob.domain.article_tag.ArticleTagRepository;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.member.GenderType;
import com.hongxeob.domain.member.Member;
import com.hongxeob.domain.member.Role;
import com.hongxeob.domain.tag.Tag;
import com.hongxeob.image.ImageService;
import com.hongxeob.image.dto.FileDto;
import com.hongxeob.image.dto.req.ImageUploadReq;
import com.hongxeob.member.MemberService;
import com.hongxeob.tag.TagService;


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
