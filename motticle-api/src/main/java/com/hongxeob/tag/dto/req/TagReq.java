package com.hongxeob.tag.dto.req;


import com.hongxeob.domain.tag.Tag;

public record TagReq(
	String name
) {
	public static Tag toTag(TagReq req) {
		return Tag.builder()
			.name(req.name)
			.build();
	}
}
