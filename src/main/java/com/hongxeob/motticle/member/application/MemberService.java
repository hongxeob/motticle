package com.hongxeob.motticle.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.global.error.exception.EntityNotFoundException;
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

	private final MemberRepository memberRepository;

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
		System.out.println("findMember = " + findMember.getNickname());
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

	private Member getMember(Long id) {
		return memberRepository.findById(id).orElseThrow(() -> {
			log.warn("GET:READ:NOT_FOUND_MEMBER_BY_ID : {}", id);
			return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
		});
	}
}
