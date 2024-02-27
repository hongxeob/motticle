package com.hongxeob.motticle.report.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hongxeob.motticle.article.domain.Article;
import com.hongxeob.motticle.member.domain.Member;

public interface ReportRepository extends JpaRepository<Report, Long> {

	boolean existsByRequesterAndArticle(Member requester, Article article);
}
