package com.hongxeob.motticle.auth.application.dto;

import com.hongxeob.motticle.member.domain.Member;

public record SecurityMemberDto(
	String email,
	String role,
	String nickname,
	Long memberId
) {

	public static SecurityMemberDto from(Member member) {
		return new SecurityMemberDto(member.getEmail(), member.getRole().toString(), member.getNickname(), member.getId());
	}

}
