package com.hongxeob.motticle.report.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.motticle.article.application.ArticleService;
import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.member.application.MemberService;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.report.application.dto.req.ReportReq;
import com.hongxeob.motticle.report.application.dto.res.ReportRes;
import com.hongxeob.motticle.report.domain.Report;
import com.hongxeob.motticle.report.domain.ReportRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ReportService {

	private final ReportRepository reportRepository;
	private final MemberService memberService;
	private final ArticleService articleService;

	public ReportRes reportArticle(Long memberId, ReportReq req) {
		Member member = memberService.getMember(memberId);

		Article article = articleService.getArticle(req.articleId());

		article.checkArticleOwnerWithReporterId(member.getId());

		if (isAlreadyReportArticleBySameMember(member, article)) {
			log.warn("GET:READ:ALREADY_REPORTED_ARTICLE_BY_SAME_MEMBER : memberId => {}, articleId => {} ", member.getId(), article.getId());
			throw new BusinessException(ErrorCode.ALREADY_REPORTED_ARTICLE_BY_SAME_MEMBER);
		}

		Report report = Report.builder()
			.article(article)
			.requester(member)
			.content(req.content())
			.build();

		reportRepository.save(report);

		return ReportRes.from(report);
	}

	private boolean isAlreadyReportArticleBySameMember(Member member, Article article) {
		return reportRepository.existsByRequesterAndArticle(member, article);
	}
}
