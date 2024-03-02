package com.hongxeob.tag;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.exception.EntityNotFoundException;
import com.hongxeob.domain.member.Member;
import com.hongxeob.domain.tag.Tag;
import com.hongxeob.domain.tag.TagRepository;
import com.hongxeob.member.MemberService;
import com.hongxeob.tag.dto.req.TagReq;
import com.hongxeob.tag.dto.res.TagRes;
import com.hongxeob.tag.dto.res.TagsSliceRes;

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
