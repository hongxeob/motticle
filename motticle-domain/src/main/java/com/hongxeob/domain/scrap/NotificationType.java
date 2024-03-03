package com.hongxeob.domain.scrap;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

	REPORTED("신고", "부적절한 제목 혹은 내용으로 아티클이 신고당하였습니다. 확인해주세요. : %d"),
	SCRAPED("스크랩", "내 아티클이 스크랩되었어요! : %d");

	private final String text;
	private final String messageTemplate;

	public static boolean isValidNotificationType(String type) {
		return Arrays.stream(NotificationType.values())
			.anyMatch(notificationType -> notificationType.text.equals(type));
	}

	public String createMessage(Long articleId) {
		return String.format(this.messageTemplate, articleId);
	}
}
