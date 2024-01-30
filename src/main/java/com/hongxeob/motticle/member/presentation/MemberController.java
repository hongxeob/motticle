package com.hongxeob.motticle.member.presentation;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hongxeob.motticle.global.aop.CurrentMemberId;
import com.hongxeob.motticle.image.application.dto.req.ImageUploadReq;
import com.hongxeob.motticle.image.application.dto.res.ImagesRes;
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
	public ResponseEntity<MemberInfoRes> addMemberInfo(Long memberId, @RequestBody @Validated MemberInfoReq memberInfoReq) {
		MemberInfoRes memberInfoRes = memberService.registerInfo(memberId, memberInfoReq);

		return ResponseEntity.ok(memberInfoRes);
	}

	@CurrentMemberId
	@GetMapping
	public ResponseEntity<MemberInfoRes> getMember(Long memberId) {
		MemberInfoRes memberInfoRes = memberService.getInfo(memberId);

		return ResponseEntity.ok(memberInfoRes);
	}

	@CurrentMemberId
	@PatchMapping("/modify")
	public ResponseEntity<Void> updateNickname(Long memberId, @RequestBody @Validated MemberModifyReq req) {
		memberService.changeNickname(memberId, req);

		return ResponseEntity
			.noContent()
			.build();
	}

	@CurrentMemberId
	@PatchMapping("/modify/image")
	public ResponseEntity<ImagesRes> updateImage(Long memberId, @RequestPart(required = false) List<MultipartFile> image) throws IOException {
		ImageUploadReq imageUploadReq = new ImageUploadReq(image);

		ImagesRes imagesRes = memberService.updateImage(memberId, imageUploadReq);

		return ResponseEntity.ok(imagesRes);
	}

	@CurrentMemberId
	@DeleteMapping("/modify/image")
	public ResponseEntity<Void> deleteImage(Long memberId) {
		memberService.deleteImage(memberId);

		return ResponseEntity
			.noContent()
			.build();
	}

	@GetMapping("/nickname")
	public ResponseEntity<Void> checkDuplicatedNickname(@RequestParam String nickname) {
		memberService.checkDuplicatedNickname(nickname);

		return ResponseEntity
			.noContent()
			.build();
	}

	@CurrentMemberId
	@DeleteMapping
	public ResponseEntity<Void> deleteMember(Long memberId) {
		memberService.delete(memberId);

		return ResponseEntity
			.noContent()
			.build();
	}
}
