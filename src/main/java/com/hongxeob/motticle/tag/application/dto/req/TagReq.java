package com.hongxeob.motticle.tag.application.dto.req;

import com.hongxeob.motticle.tag.domain.Tag;

public record TagReq(
	String name
) {
	public static Tag toTag(TagReq req) {
		return Tag.builder()
			.name(req.name)
			.build();
	}
}
