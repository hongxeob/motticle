package com.hongxeob.notification.res;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.hongxeob.domain.notification.Notification;

public record NotificationRes(
	Long id,
	String content,
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	LocalDateTime createdAt,
	boolean isRead
) {

	public static NotificationRes from(Notification notification) {
		return new NotificationRes(
			notification.getId(),
			notification.getContent(),
			notification.getCreatedAt(),
			notification.getIsRead()
		);
	}
}
