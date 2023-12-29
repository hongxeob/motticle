package com.hongxeob.motticle.scrap.application.dto.req;

import javax.validation.constraints.NotNull;

public record ScrapReq(
	@NotNull(message = "스크랩을 위한 아티클 ID는 필수입니다.")
	Long articleId
) {
}
