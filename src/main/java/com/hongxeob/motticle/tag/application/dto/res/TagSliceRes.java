package com.hongxeob.motticle.tag.application.dto.res;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.hongxeob.motticle.tag.domain.Tag;

public record TagSliceRes(
	Long id,
	String name,
	Long memberId,

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	LocalDateTime createdAt,

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	LocalDateTime updatedAt
) {

	public static TagSliceRes from(Tag tag) {
		return new TagSliceRes(tag.getId(), tag.getName(),
			tag.getMember().getId(),
			tag.getCreatedAt(), tag.getUpdatedAt());
	}
}
