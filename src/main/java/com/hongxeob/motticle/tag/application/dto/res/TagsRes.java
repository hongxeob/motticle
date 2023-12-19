package com.hongxeob.motticle.tag.application.dto.res;

import java.util.List;

import com.hongxeob.motticle.tag.domain.Tag;

public record TagsRes(
	List<TagRes> tagRes
) {

	public static TagsRes from(List<Tag> tagList) {
		return new TagsRes(tagList.stream()
			.map(TagRes::from)
			.toList());
	}
}
