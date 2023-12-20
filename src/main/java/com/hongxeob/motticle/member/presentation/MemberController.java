package com.hongxeob.motticle.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hongxeob.motticle.global.aop.CurrentMemberId;
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

	@CurrentMemberId
	@PatchMapping
	public ResponseEntity<MemberInfoRes> registerInfo(Long memberId, @RequestBody @Validated MemberInfoReq req) {
		MemberInfoRes memberInfoRes = memberService.registerInfo(memberId, req);

		return ResponseEntity.ok(memberInfoRes);
	}

	@CurrentMemberId
	@PatchMapping("/modify")
	public ResponseEntity<Void> modifyNickname(Long memberId, @RequestBody @Validated MemberModifyReq req) {
		memberService.changeNickname(memberId, req);

		return ResponseEntity
			.noContent()
			.build();
	}

	@GetMapping("/nickname")
	public ResponseEntity<Void> checkDuplicatedNickname(@RequestBody @Validated MemberModifyReq req) {
		memberService.checkDuplicatedNickname(req);

		return ResponseEntity
			.noContent()
			.build();
	}

	@CurrentMemberId
	@DeleteMapping
	public ResponseEntity<Void> delete(Long memberId) {
		memberService.delete(memberId);

		return ResponseEntity
			.noContent()
			.build();
	}
}
