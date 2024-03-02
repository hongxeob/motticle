package com.hongxeob.auth.dto;

import com.hongxeob.domain.member.Member;

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
