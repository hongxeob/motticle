package com.hongxeob.scrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;

import com.hongxeob.article.ArticleService;
import com.hongxeob.article.dto.res.ArticlesOgRes;
import com.hongxeob.common.util.BucketUtils;
import com.hongxeob.domain.article.Article;
import com.hongxeob.domain.article.ArticleType;
import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.member.GenderType;
import com.hongxeob.domain.member.Member;
import com.hongxeob.domain.member.Role;
import com.hongxeob.domain.scrap.Scrap;
import com.hongxeob.domain.scrap.ScrapRepository;
import com.hongxeob.member.MemberService;
import com.hongxeob.opengraph.OpenGraphProcessor;
import com.hongxeob.scrap.dto.req.ScrapReq;
import com.hongxeob.scrap.dto.res.ScrapRes;

@ExtendWith(MockitoExtension.class)
class ScrapServiceTest {

	@Mock
	private ScrapRepository scrapRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private ArticleService articleService;

	@Mock
	private OpenGraphProcessor openGraphProcessor;

	@Mock
	private BucketUtils bucketUtils;

	@InjectMocks
	private ScrapService scrapService;

	private Member member;
	private Article article;
	private Scrap scrap;

	@BeforeEach
	void setUp() {
		member = Member.builder()
			.id(1L)
			.email("sjun2918@naver.com")
			.nickname("호빵")
			.role(Role.GUEST)
			.genderType(GenderType.FEMALE)
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

		scrap = Scrap.builder()
			.member(member)
			.article(article)
			.build();
	}

	@Test
	@DisplayName("스크랩 성공")
	void scrapSuccessTest() throws Exception {

		//given
		ScrapReq scrapReq = new ScrapReq(article.getId());

		when(memberService.getMember(anyLong()))
			.thenReturn(member);
		when(articleService.getArticle(anyLong()))
			.thenReturn(article);
		when(scrapRepository.findByMemberIdAndArticleId(anyLong(), anyLong()))
			.thenReturn(Optional.empty());
		when(scrapRepository.save(any()))
			.thenReturn(scrap);

		//when
		ScrapRes scrapRes = scrapService.scrap(1L, scrapReq);

		//then
		assertThat(article.getScrapCount()).isEqualTo(1);
		assertThat(scrapRes.memberId()).isEqualTo(member.getId());
	}

	@Test
	@DisplayName("스크랩 실패_비공개 아티클")
	void scrapFailTest_articleIsPrivate() throws Exception {

		//given
		ScrapReq scrapReq = new ScrapReq(article.getId());
		article.updatePublicStatus();

		when(memberService.getMember(anyLong())).thenReturn(member);
		when(articleService.getArticle(anyLong())).thenReturn(article);

		//when -> then
		assertThrows(BusinessException.class,
			() -> scrapService.scrap(1L, scrapReq), ErrorCode.ARTICLE_IS_PRIVATE.getMessage());
	}

	@Test
	@DisplayName("스크랩 삭제 성공")
	void removeScrapSuccessTest() {
		// Given
		when(memberService.getMember(anyLong())).thenReturn(member);
		when(articleService.getArticle(anyLong())).thenReturn(article);
		when(scrapRepository.findByMemberIdAndArticleId(anyLong(), anyLong())).thenReturn(Optional.of(scrap));

		// When
		assertDoesNotThrow(() -> scrapService.removeScrap(1L, 1L));

		// Then
		assertThat(article.getScrapCount()).isEqualTo(0);
		verify(scrapRepository, times(1))
			.delete(any(Scrap.class));
	}

	@Test
	@DisplayName("스크랩 삭제 실패 - 존재하지 않는 스크랩")
	void removeScrapFailureScrapNotFoundTest() {
		// Given
		when(memberService.getMember(anyLong()))
			.thenReturn(member);
		when(articleService.getArticle(anyLong()))
			.thenReturn(article);
		when(scrapRepository.findByMemberIdAndArticleId(anyLong(), anyLong()))
			.thenReturn(Optional.empty());

		// When - Then
		BusinessException exception = assertThrows(BusinessException.class, () -> scrapService.removeScrap(1L, 1L));
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_REQUEST_TAG_IN_ARTICLE);
	}

	@Test
	@DisplayName("스크랩한 모든 아티클 조회 성공")
	void getAllScrapedArticleSuccessTest() {
		// Given
		Slice<Article> mockedSlice = mock(Slice.class);
		ArticlesOgRes mockedArticlesOgRes = new ArticlesOgRes(new ArrayList<>(), true);

		when(memberService.getMember(anyLong()))
			.thenReturn(member);
		when(scrapRepository.findAllArticlesByMemberId(anyLong()))
			.thenReturn(mockedSlice);
		when(openGraphProcessor.generateArticlesOgResWithOpenGraph(any()))
			.thenReturn(mockedArticlesOgRes);

		// When
		ArticlesOgRes articlesOgRes = scrapService.getAllScrapedArticle(1L);

		// Then
		assertNotNull(articlesOgRes);
	}

	@Test
	@DisplayName("스크랩 여부 확인 - 스크랩 O")
	void checkScrapedArticleSuccessTest() {
		// Given
		when(memberService.getMember(anyLong()))
			.thenReturn(member);
		when(articleService.getArticle(anyLong()))
			.thenReturn(article);
		when(scrapRepository.existsByMemberIdAndArticleId(anyLong(), anyLong()))
			.thenReturn(true);

		// When
		boolean result = scrapService.isScrapedArticle(1L, 1L);

		// Then
		assertTrue(result);
	}

	@Test
	@DisplayName("스크랩 여부 확인 - 스크랩 X")
	void checkScrapedArticleFailureTest() {
		// Given
		when(memberService.getMember(anyLong()))
			.thenReturn(member);
		when(articleService.getArticle(anyLong()))
			.thenReturn(article);
		when(scrapRepository.existsByMemberIdAndArticleId(anyLong(), anyLong()))
			.thenReturn(false);

		// When
		boolean result = scrapService.isScrapedArticle(1L, 1L);

		// Then
		assertFalse(result);
	}
}
