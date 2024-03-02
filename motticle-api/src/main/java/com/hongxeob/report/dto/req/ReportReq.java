package com.hongxeob.report.dto.req;

import javax.validation.constraints.NotBlank;

public record ReportReq(
	Long articleId,
	@NotBlank(message = "신고 사유를 입력해주세요.")
	String content
) {
}
