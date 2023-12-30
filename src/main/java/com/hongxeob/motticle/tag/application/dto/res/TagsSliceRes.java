package com.hongxeob.motticle.tag.application.dto.res;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.hongxeob.motticle.tag.domain.Tag;

public record TagsSliceRes(
	List<TagSliceRes> tagSliceRes,
	boolean hasNext) {

	public static TagsSliceRes from(Slice<Tag> tagsSlice) {
		List<TagSliceRes> tagSliceRes = tagsSlice.stream()
			.map(TagSliceRes::from)
			.toList();
		return new TagsSliceRes(tagSliceRes, tagsSlice.hasNext());
	}
}
