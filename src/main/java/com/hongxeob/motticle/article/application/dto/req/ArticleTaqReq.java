package com.hongxeob.motticle.article.application.dto.req;

import javax.validation.constraints.NotNull;

public record ArticleTaqReq(
	@NotNull(message = "태그 id는 필수 입력 입니다.")
	Long tagId
) {
}
