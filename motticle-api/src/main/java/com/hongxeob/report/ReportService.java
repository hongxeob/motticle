package com.hongxeob.report;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.article.ArticleService;
import com.hongxeob.domain.article.Article;
import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.member.Member;
import com.hongxeob.domain.report.Report;
import com.hongxeob.domain.report.ReportRepository;
import com.hongxeob.member.MemberService;
import com.hongxeob.report.dto.req.ReportReq;
import com.hongxeob.report.dto.res.ReportRes;

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
