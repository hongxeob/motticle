package com.hongxeob.motticle.member.application.dto.res;

import com.hongxeob.motticle.member.domain.Member;

public record MemberInfoRes(
	Long id,
	String email,
	String nickname,
	String genderType,
	String role
) {

	public static MemberInfoRes from(Member member) {
		return new MemberInfoRes(member.getId(), member.getEmail(), member.getNickname(), member.getGenderType().name(), member.getRole().getKey());
	}
}
