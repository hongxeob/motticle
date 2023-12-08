package com.hongxeob.motticle.article.domain;

import java.util.ArrayList;
import java.util.List;

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

	@ManyToOne
	@JoinColumn(name = "member_id")
	private Member member;

	@OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
	private List<ArticleTag> articleTags = new ArrayList<>();

	@Builder
	public Article(String title, ArticleType type, String content, String memo, boolean isPublic, Member member) {
		this.title = title;
		this.type = type;
		this.content = content;
		this.memo = memo;
		this.isPublic = isPublic;
	}

	public void writeBy(Member member) {
		this.member = member;
	}
}
