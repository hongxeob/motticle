package com.hongxeob.motticle.tag.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.global.error.exception.EntityNotFoundException;
import com.hongxeob.motticle.member.application.MemberService;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.tag.application.dto.req.TagReq;
import com.hongxeob.motticle.tag.application.dto.res.TagRes;
import com.hongxeob.motticle.tag.application.dto.res.TagsRes;
import com.hongxeob.motticle.tag.domain.Tag;
import com.hongxeob.motticle.tag.domain.TagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TagService {

	private final TagRepository tagRepository;
	private final MemberService memberService;

	public TagRes register(Long memberId, TagReq tagReq) {
		Member member = memberService.getMember(memberId);

		tagRepository.findByMemberIdAndName(member.getId(), tagReq.name())
			.ifPresent(tag -> {
				log.warn("GET:READ:ALREADY_REGISTERED_BY_MEMBER_ID : memberId => {}, tagName: {} ", member.getId(), tagReq.name());
				throw new BusinessException(ErrorCode.ALREADY_REGISTERED_BY_MEMBERS);
			});

		Tag tag = TagReq.toTag(tagReq);
		tag.createdBy(member);

		return TagRes.from(tag);
	}

	@Transactional(readOnly = true)
	public Tag getTag(Long id) {
		Tag tag = findTag(id);

		return tag;
	}

	@Transactional(readOnly = true)
	public TagsRes findAllByMemberId(Long memberId) {
		Member member = memberService.getMember(memberId);

		List<Tag> tagList = tagRepository.findAllByMemberId(member.getId());

		return TagsRes.from(tagList);
	}

	public void delete(Long memberId, Long id) {
		Member member = memberService.getMember(memberId);

		Tag tag = findTag(id);
		tag.checkTagOwnerWithRequesterId(member.getId());

		tagRepository.delete(tag);
	}

	private Tag findTag(Long id) {
		return tagRepository.findById(id)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_TAG_BY_ID : {}", id);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_TAG);
			});
	}

}
