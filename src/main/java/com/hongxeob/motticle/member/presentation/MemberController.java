package com.hongxeob.motticle.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hongxeob.motticle.member.application.MemberService;
import com.hongxeob.motticle.member.application.dto.req.MemberInfoReq;
import com.hongxeob.motticle.member.application.dto.req.MemberModifyReq;
import com.hongxeob.motticle.member.application.dto.res.MemberInfoRes;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

	private final MemberService memberService;

	@PatchMapping("/{id}")
	public ResponseEntity<MemberInfoRes> registerInfo(@PathVariable Long id, @RequestBody @Validated MemberInfoReq req) {
		MemberInfoRes memberInfoRes = memberService.registerInfo(id, req);

		return ResponseEntity.ok(memberInfoRes);
	}

	@PatchMapping("/modify/{id}")
	public ResponseEntity<Void> modifyNickname(@PathVariable Long id, @RequestBody @Validated MemberModifyReq req) {
		memberService.changeNickname(id, req);

		return ResponseEntity
			.noContent()
			.build();
	}
}
