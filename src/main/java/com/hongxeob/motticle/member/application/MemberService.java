package com.hongxeob.motticle.member.application;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.global.error.exception.EntityNotFoundException;
import com.hongxeob.motticle.image.application.ImageService;
import com.hongxeob.motticle.image.application.dto.req.ImageUploadReq;
import com.hongxeob.motticle.image.application.dto.res.ImagesRes;
import com.hongxeob.motticle.member.application.dto.req.MemberInfoReq;
import com.hongxeob.motticle.member.application.dto.req.MemberModifyReq;
import com.hongxeob.motticle.member.application.dto.res.MemberInfoRes;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {

	private static final String DEFAULT_PATH = "/Users/hongxeob/Desktop/simple-user-default-icon-free-png.webp";

	private final MemberRepository memberRepository;
	private final ImageService imageService;

	public MemberInfoRes registerInfo(Long id, MemberInfoReq req, ImageUploadReq imageUploadReq) throws IOException {
		Member member = getMember(id);

		List<String> image = saveImage(imageUploadReq);
		Member updatedMember = MemberInfoReq.toMember(req, image.get(0));

		member.registerInfo(updatedMember);

		return MemberInfoRes.from(member);
	}

	@Transactional(readOnly = true)
	public MemberInfoRes getInfo(Long id) {
		Member member = getMember(id);

		return MemberInfoRes.from(member);
	}

	public void changeNickname(Long id, MemberModifyReq modifyReq) {
		Member findMember = getMember(id);

		checkDuplicatedNickname(modifyReq);

		findMember.updatedNickname(modifyReq.nickname());
	}

	public ImagesRes updateImage(Long memberId, ImageUploadReq imageUploadReq) throws IOException {
		Member member = getMember(memberId);
		List<String> image = saveImage(imageUploadReq);

		member.updateImage(image.get(0));

		return ImagesRes.from(image);
	}

	public void deleteImage(Long memberId) {
		Member member = getMember(memberId);
		String image = member.getImage();

		if (Objects.equals(image, DEFAULT_PATH)) {
			log.warn("DELETE:DELETE:DEFAULT_IMAGE_ALREADY_SET : {}", member.getId());
			throw new BusinessException(ErrorCode.DEFAULT_IMAGE_ALREADY_SET);
		}

		imageService.delete(image);
		member.updateImage(DEFAULT_PATH);
	}

	public void delete(Long id) {
		Member member = getMember(id);

		memberRepository.delete(member);
	}

	@Transactional(readOnly = true)
	public void checkDuplicatedNickname(MemberModifyReq nicknameReq) {
		memberRepository.findByNickname(nicknameReq.nickname()).ifPresent(member -> {
			log.warn("GET:READ:DUPLICATED_NICKNAME : {}", nicknameReq.nickname());
			throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
		});
	}

	@Transactional(readOnly = true)
	public Member getMember(Long id) {
		return memberRepository.findById(id).orElseThrow(() -> {
			log.warn("GET:READ:NOT_FOUND_MEMBER_BY_ID : {}", id);
			return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
		});
	}

	private List<String> saveImage(ImageUploadReq imageUploadReq) throws IOException {
		if (imageUploadReq.file() != null) {
			return imageService.add(imageUploadReq.file());
		}

		return Collections.singletonList(DEFAULT_PATH);
	}
}
