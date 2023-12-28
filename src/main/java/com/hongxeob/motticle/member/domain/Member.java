package com.hongxeob.motticle.member.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.hongxeob.motticle.global.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "members")
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 30)
	private String nickname;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender_type")
	private GenderType genderType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Builder
	public Member(Long id, String nickname, String email, GenderType genderType, Role role) {
		this.id = id;
		this.nickname = nickname;
		this.email = email;
		this.genderType = genderType;
		this.role = role;
	}

	public void updatedNickname(String nickname) {
		this.nickname = nickname;
	}

	public void registerInfo(Member member) {
		this.nickname = member.getNickname();
		this.genderType = member.getGenderType();
		this.role = member.getRole();
	}

	public void updateRole(Role role) {
		this.role = role;
	}
}
