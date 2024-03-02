package com.hongxeob.member;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.domain.member.Member;
import com.hongxeob.domain.member.MemberRepository;
import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.exception.EntityNotFoundException;
import com.hongxeob.image.ImageService;
import com.hongxeob.image.dto.FileDto;
import com.hongxeob.image.dto.req.ImageUploadReq;
import com.hongxeob.image.dto.res.ImagesRes;
import com.hongxeob.member.dto.req.MemberInfoReq;
import com.hongxeob.member.dto.req.MemberModifyReq;
import com.hongxeob.member.dto.res.MemberInfoRes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {

	private static final String DEFAULT_IMAGE_PATH = "https://kr.object.ncloudstorage.com/motticle-file-storage/Default-Profile-Picture-PNG-Download-Image.png";

	private final MemberRepository memberRepository;
	private final ImageService imageService;

	public MemberInfoRes registerInfo(Long id, MemberInfoReq req) {
		Member member = getMember(id);
		Member updatedMember = MemberInfoReq.toMember(req);

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

		checkDuplicatedNickname(modifyReq.nickname());

		findMember.updatedNickname(modifyReq.nickname());
	}

	public ImagesRes updateImage(Long memberId, ImageUploadReq imageUploadReq) throws IOException {
		Member member = getMember(memberId);
		String imageUrl = saveImage(imageUploadReq);

		member.updateImage(imageUrl);

		return ImagesRes.from(imageUrl);
	}

	public void deleteImage(Long memberId) {
		Member member = getMember(memberId);
		String imageUrl = member.getImage();

		if (Objects.equals(imageUrl, DEFAULT_IMAGE_PATH)) {
			log.warn("DELETE:DELETE:DEFAULT_IMAGE_ALREADY_SET : {}", member.getId());
			throw new BusinessException(ErrorCode.DEFAULT_IMAGE_ALREADY_SET);
		}

		imageService.deleteFile(imageUrl);
		member.updateImage(DEFAULT_IMAGE_PATH);
	}

	public void delete(Long id) {
		Member member = getMember(id);

		memberRepository.delete(member);
	}

	@Transactional(readOnly = true)
	public void checkDuplicatedNickname(String nickname) {
		memberRepository.findByNickname(nickname).ifPresent(member -> {
			log.warn("GET:READ:DUPLICATED_NICKNAME : {}", nickname);
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

	private String saveImage(ImageUploadReq imageUploadReq) throws IOException {
		if (imageUploadReq.file() != null) {
			List<FileDto> fileDtos = imageService.uploadFiles(imageUploadReq.file());

			return fileDtos.get(0).getUploadFileUrl();
		}

		return DEFAULT_IMAGE_PATH;
	}
}
