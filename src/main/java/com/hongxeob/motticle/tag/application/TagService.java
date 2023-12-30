package com.hongxeob.motticle.tag.application;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.global.error.exception.EntityNotFoundException;
import com.hongxeob.motticle.member.application.MemberService;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.tag.application.dto.req.TagReq;
import com.hongxeob.motticle.tag.application.dto.res.TagRes;
import com.hongxeob.motticle.tag.application.dto.res.TagsSliceRes;
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

		tagRepository.save(tag);

		return TagRes.from(tag);
	}

	@Transactional(readOnly = true)
	public Tag getTag(Long id) {
		Tag tag = findTag(id);

		return tag;
	}

	@Transactional(readOnly = true)
	public TagsSliceRes findAllByMemberId(Long memberId, Pageable pageable) {
		Member member = memberService.getMember(memberId);

		Slice<Tag> tagSlice = tagRepository.findAllByMemberId(member.getId(), pageable);

		return TagsSliceRes.from(tagSlice);
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
