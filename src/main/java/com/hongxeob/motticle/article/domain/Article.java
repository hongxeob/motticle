package com.hongxeob.motticle.article.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.hongxeob.motticle.article_tag.domain.ArticleTag;
import com.hongxeob.motticle.global.BaseEntity;
import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.member.domain.Member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "articles")
public class Article extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Enumerated(EnumType.STRING)
	private ArticleType type;

	@Lob
	private String content;

	@Lob
	private String memo;

	@Column(name = "is_public")
	private boolean isPublic;

	@Column(name = "scrap_count")
	private Long scrapCount;

	@ManyToOne
	@JoinColumn(name = "member_id")
	private Member member;

	@OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
	private Set<ArticleTag> articleTags = new HashSet<>();

	@Builder
	public Article(Long id, String title, ArticleType type, String content, String memo, boolean isPublic, Member member) {
		this.id = id;
		this.title = title;
		this.type = type;
		this.content = content;
		this.memo = memo;
		this.scrapCount = 0L;
		this.isPublic = isPublic;
		this.member = member;
	}

	public void writeBy(Member member) {
		this.member = member;
	}

	public void addTag(ArticleTag tag) {
		if (!articleTags.contains(tag)) {
			articleTags.add(tag);
		}
	}

	public void removeTag(ArticleTag tag) {
		articleTags.remove(tag);
	}

	public void setFilePath(String filePath) {
		this.content = filePath;
	}

	public void increaseScrapCount() {
		this.scrapCount++;
	}

	public void decreaseScrapCount() {
		if (this.scrapCount <= 0) {
			this.scrapCount = 0L;
		} else {
			this.scrapCount--;
		}
	}

	public void checkArticleOwnerWithRequesterId(Long requesterId) {
		if (!this.member.getId().equals(requesterId)) {
			throw new BusinessException(ErrorCode.TAG_OWNER_AND_REQUESTER_ARE_DIFFERENT);
		}
	}

	public void updateInfo(Article article) {
		this.title = article.title;
		this.content = article.content;
		this.memo = article.memo;
		this.isPublic = article.isPublic;
	}

	public void updatePublicStatus() {
		this.isPublic = !this.isPublic;
	}
}
