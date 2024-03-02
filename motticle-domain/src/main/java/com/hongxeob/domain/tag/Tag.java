package com.hongxeob.domain.tag;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.hongxeob.domain.article_tag.ArticleTag;
import com.hongxeob.domain.common.jpa.BaseEntity;
import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.member.Member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tags")
public class Tag extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
	private List<ArticleTag> articleTags = new ArrayList<>();

	@Builder
	public Tag(Long id, String name, Member member) {
		this.id = id;
		this.name = name;
		this.member = member;
	}

	public void createdBy(Member member) {
		this.member = member;
	}

	public void checkTagOwnerWithRequesterId(Long requesterId) {
		if (!this.member.getId().equals(requesterId)) {
			throw new BusinessException(ErrorCode.TAG_OWNER_AND_REQUESTER_ARE_DIFFERENT);
		}
	}
}
