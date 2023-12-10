package com.hongxeob.motticle.auth.application.dto;

import com.hongxeob.motticle.member.domain.Member;

public record SecurityMemberDto(
	Long memberId,
	String email,
	String role,
	String nickname
) {

	public static SecurityMemberDto from(Member member) {
		return new SecurityMemberDto(member.getId(), member.getEmail(), member.getRole().toString(), member.getNickname());
	}
}
