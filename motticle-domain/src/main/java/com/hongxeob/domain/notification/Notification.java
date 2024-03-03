package com.hongxeob.domain.notification;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hongxeob.domain.article.Article;
import com.hongxeob.domain.common.jpa.BaseEntity;
import com.hongxeob.domain.member.Member;
import com.hongxeob.domain.scrap.NotificationType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "article_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Article article;

	@Enumerated(EnumType.STRING)
	private NotificationType notificationType;

	private Boolean isRead = Boolean.FALSE;

	@Builder
	public Notification(String content, Member member, Article article, NotificationType notificationType) {
		this.content = content;
		this.member = member;
		this.article = article;
		this.notificationType = notificationType;
	}

	public void markAsRead() {
		this.isRead = true;
	}

	public static String createMessage(NotificationType notificationType, Article article) {
		return notificationType.createMessage(article.getId());
	}
}
