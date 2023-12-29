package com.hongxeob.motticle.member.application.dto.req;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.hongxeob.motticle.member.domain.GenderType;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.Role;

public record MemberInfoReq(
	@NotBlank(message = "닉네임은 비어있을 수 없습니다.")
	@Pattern(regexp = "^[a-zA-Z0-9가-힣]+", message = "닉네임은 한글, 알파벳, 숫자만 사용할 수 있습니다.")
	String nickname,
	String genderType
) {

	public static Member toMember(MemberInfoReq req, String image) {
		return Member.builder()
			.nickname(req.nickname)
			.genderType(GenderType.of(req.genderType()))
			.role(Role.USER)
			.image(image)
			.build();
	}
}
