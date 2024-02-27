package com.hongxeob.motticle.report.application.dto.res;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.hongxeob.motticle.report.domain.Report;

public record ReportResponse(
	Long id,
	Long articleId,
	Long reporterId,
	String content,
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	LocalDateTime createdDatetime
) {

	public static ReportResponse from(Report report) {
		return new ReportResponse(
			report.getId(),
			report.getArticle().getId(),
			report.getRequester().getId(),
			report.getContent(),
			report.getArticle().getCreatedAt());
	}
}
