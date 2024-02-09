package com.hongxeob.motticle.article.application.dto.res;

import com.hongxeob.motticle.article.opengraph.OpenGraphVO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenGraphResponse {
	private int code;
	private String description;
	private String title;
	private String url;
	private String image;

	public static OpenGraphResponse of(int code, OpenGraphVO openGraphVO) {
		return OpenGraphResponse.builder()
			.code(code)
			.image(openGraphVO.getImage())
			.title(openGraphVO.getTitle())
			.url(openGraphVO.getUrl())
			.description(openGraphVO.getDescription())
			.build();
	}

	public static OpenGraphResponse from(int code) {
		return OpenGraphResponse.builder()
			.code(code)
			.build();
	}
}
