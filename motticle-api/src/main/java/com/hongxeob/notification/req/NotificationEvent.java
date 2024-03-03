package com.hongxeob.notification.req;

import com.hongxeob.domain.article.Article;
import com.hongxeob.domain.scrap.NotificationType;

public record NotificationEvent(
	NotificationType notificationType,
	Long memberId,
	Long ArticleId
) {

	public static NotificationEvent from(NotificationType type, Article article) {
		return new NotificationEvent(type, article.getMember().getId(), article.getId());
	}
}
