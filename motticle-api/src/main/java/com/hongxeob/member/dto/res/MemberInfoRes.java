package com.hongxeob.member.dto.res;

import com.hongxeob.domain.member.Member;

public record MemberInfoRes(
	Long id,
	String email,
	String nickname,
	String genderType,
	String role,
	String image
) {

	public static MemberInfoRes from(Member member) {
		return new MemberInfoRes(member.getId(), member.getEmail(), member.getNickname(),
			member.getGenderType().name(), member.getRole().getKey(), member.getImage());
	}
}
