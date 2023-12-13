package com.hongxeob.motticle.member.application.dto.req;

import com.hongxeob.motticle.member.domain.GenderType;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.Role;

public record MemberInfoReq(
	String nickname,
	String genderType
) {

	public static Member toMember(MemberInfoReq req) {
		return Member.builder()
			.nickname(req.nickname)
			.genderType(GenderType.of(req.genderType()))
			.role(Role.USER)
			.build();
	}
}
