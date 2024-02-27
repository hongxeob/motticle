package com.hongxeob.motticle.report.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hongxeob.motticle.article.application.ArticleService;
import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.article.domain.ArticleType;
import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.member.application.MemberService;
import com.hongxeob.motticle.member.domain.GenderType;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.Role;
import com.hongxeob.motticle.report.application.dto.req.ReportReq;
import com.hongxeob.motticle.report.application.dto.res.ReportResponse;
import com.hongxeob.motticle.report.domain.Report;
import com.hongxeob.motticle.report.domain.ReportRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

	@Mock
	private ReportRepository reportRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private ArticleService articleService;

	@InjectMocks
	private ReportService reportService;

	private Member member;
	private Member member2;
	private Article article;
	private Report report;

	@BeforeEach
	void setUp() {
		member = Member.builder()
			.id(1L)
			.email("sjun2918@naver.com")
			.nickname("호빵")
			.role(Role.GUEST)
			.genderType(GenderType.FEMALE)
			.build();

		member2 = Member.builder()
			.id(12L)
			.email("sjun291822@naver.com")
			.nickname("호빵2")
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

		report = Report.builder()
			.requester(member2)
			.content("욕설이 너무 많아요.")
			.article(article)
			.build();
	}

	@Test
	@DisplayName("아티클 신고 성공")
	void reportSuccessTest() throws Exception {

		//given
		ReportReq req = new ReportReq(article.getId(), "욕설이 너무 많아요");

		when(memberService.getMember(anyLong()))
			.thenReturn(member2);
		when(articleService.getArticle(anyLong()))
			.thenReturn(article);
		when(reportRepository.existsByRequesterAndArticle(any(), any()))
			.thenReturn(false);
		when(reportRepository.save(any()))
			.thenReturn(report);

		//when
		ReportResponse res = reportService.reportArticle(member2.getId(), req);

		//then
		assertThat(res.reporterId()).isEqualTo(member2.getId());
		assertThat(res.content()).isEqualTo(req.content());
	}

	@Test
	@DisplayName("아티클 신고 실패 - 자신의 아티클을 신고")
	void reportFailTest_ownArticleReport() throws Exception {

		//given
		ReportReq req = new ReportReq(article.getId(), "욕설이 너무 많아요");

		when(memberService.getMember(anyLong()))
			.thenReturn(member);
		when(articleService.getArticle(anyLong()))
			.thenReturn(article);

		//when -> then
		assertThrows(BusinessException.class,
			() -> reportService.reportArticle(member.getId(), req));
	}

	@Test
	@DisplayName("아티클 신고 실패 - 이미 신고한 아티클")
	void reportFailTest_alreadyReport() throws Exception {

		//given
		ReportReq req = new ReportReq(article.getId(), "욕설이 너무 많아요");

		when(memberService.getMember(anyLong()))
			.thenReturn(member2);
		when(articleService.getArticle(anyLong()))
			.thenReturn(article);
		when(reportRepository.existsByRequesterAndArticle(any(), any()))
			.thenReturn(true);

		//when -> then
		assertThrows(BusinessException.class,
			() -> reportService.reportArticle(member2.getId(), req));
	}
}
