package com.hongxeob.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.member.GenderType;
import com.hongxeob.domain.member.Member;
import com.hongxeob.domain.member.Role;
import com.hongxeob.domain.tag.Tag;
import com.hongxeob.domain.tag.TagRepository;
import com.hongxeob.member.MemberService;
import com.hongxeob.tag.dto.req.TagReq;
import com.hongxeob.tag.dto.res.TagRes;
import com.hongxeob.tag.dto.res.TagsSliceRes;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

	@Mock
	private TagRepository tagRepository;

	@Mock
	private MemberService memberService;

	@InjectMocks
	private TagService tagService;

	private Member member;
	private Member member2;
	private Tag tag;

	@BeforeEach
	void setUp() {
		member = Member.builder()
			.id(1L)
			.email("sjun2918@naver.com")
			.nickname("호빵")
			.role(Role.GUEST)
			.genderType(null)
			.build();

		member2 = Member.builder()
			.id(2L)
			.email("sjun29128@naver.com")
			.nickname("호빵2")
			.role(Role.GUEST)
			.genderType(GenderType.MALE)
			.build();

		tag = Tag.builder()
			.id(9L)
			.name("미디어")
			.member(member)
			.build();
	}

	@Test
	@DisplayName("태그 단건 등록 성공")
	void registerSuccessTest() throws Exception {

		//given
		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		TagReq tagReq = new TagReq("IT");

		//when
		TagRes tagRes = tagService.register(member.getId(), tagReq);

		//then
		assertThat(tagRes.memberId()).isEqualTo(member.getId());
		assertThat(tagRes.name()).isEqualTo(tagReq.name());
	}

	@Test
	@DisplayName("태그 등록 실패 - 이미 회원이 등록한 태그")
	void registerFailTest_alreadyRegistered() throws Exception {

		//given
		TagReq tagReq = new TagReq("IT");
		Tag tag = Tag.builder()
			.name("IT")
			.build();

		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(tagRepository.findByMemberIdAndName(member.getId(), tagReq.name()))
			.thenReturn(Optional.of(tag));

		//when -> then
		assertThrows(BusinessException.class,
			() -> tagService.register(member.getId(), tagReq));
	}

	@Test
	@DisplayName("태그 단건 조회")
	void getTagSuccessTest() throws Exception {

		//given
		when(tagRepository.findById(tag.getId()))
			.thenReturn(Optional.of(tag));

		//when
		Tag foundTag = tagService.getTag(tag.getId());

		//then
		assertThat(foundTag.getName()).isEqualTo(tag.getName());
	}

	@Test
	@DisplayName("고객이 보유한 태그 전체 조회")
	void findAllByMemberIdSuccessTest() throws Exception {

		//given
		Pageable pageable = PageRequest.of(0, 20);
		List<Tag> tagList = List.of(tag);
		Slice<Tag> tagSlice = new PageImpl<>(tagList, pageable, tagList.size());

		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(tagRepository.findAllByMemberId(member.getId(), pageable))
			.thenReturn(tagSlice);

		//when
		TagsSliceRes tagsRes = tagService.findAllByMemberId(member.getId(), pageable);

		//then
		assertThat(tagsRes.tagSliceRes().get(0).name()).isEqualTo(tag.getName());
	}

	@Test
	@DisplayName("태그 삭제 성공")
	void deleteSuccessTest() throws Exception {

		//given
		when(memberService.getMember(member.getId()))
			.thenReturn(member);
		when(tagRepository.findById(tag.getId()))
			.thenReturn(Optional.of(tag));

		//when -> then
		assertDoesNotThrow(() -> tagService.delete(member.getId(), tag.getId()));
	}

	@Test
	@DisplayName("태그 삭제 실패 - 소유자와 요청자가 다름")
	void deleteFailTest_notMatchedMemberId() throws Exception {

		//given
		when(memberService.getMember(member2.getId()))
			.thenReturn(member2);
		when(tagRepository.findById(tag.getId()))
			.thenReturn(Optional.of(tag));

		//when -> then
		assertThrows(BusinessException.class,
			() -> tagService.delete(member2.getId(), tag.getId()));
	}
}
