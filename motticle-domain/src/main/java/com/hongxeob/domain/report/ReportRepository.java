package com.hongxeob.domain.report;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hongxeob.domain.article.Article;
import com.hongxeob.domain.member.Member;

public interface ReportRepository extends JpaRepository<Report, Long> {

	boolean existsByRequesterAndArticle(Member requester, Article article);
}
