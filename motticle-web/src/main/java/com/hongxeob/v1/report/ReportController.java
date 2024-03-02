package com.hongxeob.v1.report;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hongxeob.common.aop.CurrentMemberId;
import com.hongxeob.report.ReportService;
import com.hongxeob.report.dto.req.ReportReq;
import com.hongxeob.report.dto.res.ReportRes;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	@PostMapping
	@CurrentMemberId
	public ResponseEntity<ReportRes> reportArticle(Long memberId, @RequestBody @Validated ReportReq req) {
		ReportRes reportRes = reportService.reportArticle(memberId, req);

		return ResponseEntity.ok(reportRes);
	}
}
