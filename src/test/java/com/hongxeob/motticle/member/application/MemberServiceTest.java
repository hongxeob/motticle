package com.hongxeob.motticle.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.springframework.web.multipart.MultipartFile;

import com.hongxeob.motticle.global.error.exception.BusinessException;
import com.hongxeob.motticle.global.error.exception.EntityNotFoundException;
import com.hongxeob.motticle.image.application.ImageService;
import com.hongxeob.motticle.image.application.dto.req.ImageUploadReq;
import com.hongxeob.motticle.member.application.dto.req.MemberInfoReq;
import com.hongxeob.motticle.member.application.dto.req.MemberModifyReq;
import com.hongxeob.motticle.member.application.dto.res.MemberInfoRes;
import com.hongxeob.motticle.member.domain.GenderType;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.MemberRepository;
import com.hongxeob.motticle.member.domain.Role;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ImageService imageService;

	@InjectMocks
	private MemberService memberService;

	private Member member;
	private Member member2;

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
	}

	@Test
	@DisplayName("회원 추가 정보 입력 성공")
	void registerSuccessTest() throws Exception {

		//given
		MultipartFile mockImage = mock(MultipartFile.class);

		ImageUploadReq imageUploadReq = new ImageUploadReq(List.of(mockImage));
		MemberInfoReq memberInfoReq = new MemberInfoReq("이호빵", "MALE");

		String imageFileName = "image_file_name";
		when(imageService.add(any()))
			.thenReturn(List.of(imageFileName));
		when(memberRepository.findById(member.getId()))
			.thenReturn(Optional.of(member));

		//when
		MemberInfoRes memberInfoRes = memberService.registerInfo(member.getId(), memberInfoReq, imageUploadReq);

		//then
		assertThat(memberInfoRes.genderType()).isEqualTo(GenderType.MALE.name());
		assertThat(memberInfoRes.nickname()).isEqualTo(memberInfoReq.nickname());
	}

	@Test
	@DisplayName("멤버 정보 조회 성공")
	void getInfoSuccessTest() throws Exception {

		//given
		when(memberRepository.findById(member2.getId()))
			.thenReturn(Optional.of(member2));

		//when
		MemberInfoRes memberInfoRes = memberService.getInfo(member2.getId());

		//then
		assertThat(memberInfoRes.id()).isEqualTo(member2.getId());
		assertThat(memberInfoRes.email()).isEqualTo(member2.getEmail());
	}

	@Test
	@DisplayName("멤버 정보 조회 실패 - 등록되지 않은 유저")
	void getInfoFailTest_notFoundMember() throws Exception {

		//given
		when(memberRepository.findById(99L))
			.thenReturn(Optional.empty());

		//when -> then
		assertThrows(EntityNotFoundException.class, () -> memberService.getInfo(99L));
	}

	@Test
	@DisplayName("닉네임 중복 검사 성공")
	void checkDuplicatedNicknameSuccessTest() throws Exception {

		//given
		String requestNickname = "호빵2";

		when(memberRepository.findByNickname(requestNickname))
			.thenReturn(Optional.of(member2));

		//when -> then
		assertThrows(BusinessException.class, () -> memberService.checkDuplicatedNickname(requestNickname));
	}

	@Test
	@DisplayName("멤버 닉네임 변경 성공")
	void modifyNicknameSuccessTest() throws Exception {

		String requestNickname = "호빵2";
		MemberModifyReq modifyReq = new MemberModifyReq(requestNickname);

		//given
		when(memberRepository.findById(member.getId()))
			.thenReturn(Optional.of(member));
		when(memberRepository.findByNickname(requestNickname))
			.thenReturn(Optional.empty());

		//when
		memberService.changeNickname(member.getId(), modifyReq);

		//then
		assertThat(member.getNickname()).isEqualTo(requestNickname);
	}

	@Test
	@DisplayName("멤버 닉네임 변경 실패 - 존재하는 닉네임")
	void modifyNicknameFailTest_duplicatedNickname() throws Exception {

		String requestNickname = "호빵2";
		MemberModifyReq modifyReq = new MemberModifyReq(requestNickname);

		//given
		when(memberRepository.findById(member.getId()))
			.thenReturn(Optional.of(member));
		when(memberRepository.findByNickname(requestNickname)).thenReturn(Optional.of(member2));

		//when -> then
		assertThrows(BusinessException.class, () -> memberService.changeNickname(member.getId(), modifyReq));
	}
}
