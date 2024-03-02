package com.hongxeob.tag.dto.res;

import java.util.List;

import com.hongxeob.domain.tag.Tag;

public record TagsRes(
	List<TagRes> tagRes
) {

	public static TagsRes from(List<Tag> tagList) {
		return new TagsRes(tagList.stream()
			.map(TagRes::from)
			.toList());
	}

	public static TagsRes fromRes(List<TagRes> tagRes) {
		return new TagsRes(tagRes);
	}
}
