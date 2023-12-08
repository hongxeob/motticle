package com.hongxeob.motticle.tag.domain;

import java.util.ArrayList;
import java.util.List;

import com.hongxeob.motticle.article_tag.domain.ArticleTag;
import com.hongxeob.motticle.member.domain.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tags")
public class Tag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
	private List<ArticleTag> articleTags = new ArrayList<>();

	public Tag(String name, Member member) {
		this.name = name;
		this.member = member;
	}

	public void createdBy(Member member) {
		this.member = member;
	}
}
