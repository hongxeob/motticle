package com.hongxeob.v1.member;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.member.GenderType;
import com.hongxeob.domain.member.Role;
import com.hongxeob.image.dto.FileDto;
import com.hongxeob.image.dto.res.ImagesRes;
import com.hongxeob.member.dto.req.MemberInfoReq;
import com.hongxeob.member.dto.req.MemberModifyReq;
import com.hongxeob.member.dto.res.MemberInfoRes;
import com.hongxeob.v1.common.ControllerTestSupport;

class MemberControllerTest extends ControllerTestSupport {

	@Test
	@DisplayName("멤버 추가 정보 입력 성공")
	void addMemberInfoSuccessTest() throws Exception {

		//given
		MemberInfoRes memberInfoRes = new MemberInfoRes(1L, "h123@naver.com", "호빵", GenderType.FEMALE.name(), Role.USER.name(), "image.path");
		MemberInfoReq memberInfoReq = new MemberInfoReq("호빵", GenderType.FEMALE.name());

		given(memberService.registerInfo(any(), any()))
			.willReturn(memberInfoRes);

		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/members")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(memberInfoReq)))
			.andExpect(status().isOk())
			.andDo(document("member/add-info-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("nickname").type(STRING).description("닉네임"),
					fieldWithPath("genderType").type(STRING).description("성별")
				),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("멤버 ID"),
					fieldWithPath("email").type(STRING).description("이메일"),
					fieldWithPath("nickname").type(STRING).description("닉네임"),
					fieldWithPath("genderType").type(STRING).description("성별"),
					fieldWithPath("role").type(STRING).description("권한"),
					fieldWithPath("image").type(STRING).description("이미지 경로")
				)
			));
	}

	@Test
	@DisplayName("멤버 추가 정보 실패 - 필수 값이 비어있다")
	void addMemberInfoFailTest_emptyInfo() throws Exception {

		//given
		MemberInfoReq memberInfoReq = new MemberInfoReq("", GenderType.FEMALE.name());

		given(memberService.registerInfo(any(), any()))
			.willThrow(new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/members")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(memberInfoReq)))
			.andExpect(status().isBadRequest())
			.andDo(document("member/add-info-fail-blank-info",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("nickname").type(STRING).description("닉네임"),
					fieldWithPath("genderType").type(STRING).description("성별")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 발생 시간"),
					fieldWithPath("code").type(STRING).description("오류 코드"),
					fieldWithPath("errors").type(ARRAY).description("오류 목록"),
					fieldWithPath("errors[].field").type(STRING).description("잘못 입력된 필드"),
					fieldWithPath("errors[].value").type(STRING).description("입력된 값"),
					fieldWithPath("errors[].reason").type(STRING).description("원인"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@Test
	@DisplayName("멤버 정보 조회")
	void getMemberSuccessTest() throws Exception {

		//given
		MemberInfoRes memberInfoRes = new MemberInfoRes(1L, "h123@naver.com", "호빵", GenderType.FEMALE.name(), Role.USER.name(), "image.path");

		given(memberService.getInfo(any()))
			.willReturn(memberInfoRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/members")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("member/get-info-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("멤버 ID"),
					fieldWithPath("email").type(STRING).description("이메일"),
					fieldWithPath("nickname").type(STRING).description("닉네임"),
					fieldWithPath("genderType").type(STRING).description("성별"),
					fieldWithPath("role").type(STRING).description("권한"),
					fieldWithPath("image").type(STRING).description("이미지 경로")
				)
			));
	}

	@Test
	@DisplayName("닉네임 중복 검사 성공")
	void checkDuplicatedNicknameSuccessTest() throws Exception {

		//given

		String nickname = "호빵전사";
		doNothing().when(memberService).checkDuplicatedNickname(nickname);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/members/nickname")
				.with(csrf().asHeader())
				.param("nickname", nickname)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNoContent())
			.andDo(document("member/check-duplicated-nickname-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					parameterWithName("nickname").description("닉네임")
				)
			));
	}

	@Test
	@DisplayName("닉네임 중복 검사 실패 - 중복된 닉네임")
	void checkDuplicatedNicknameFailTest_duplicatedNickname() throws Exception {

		//given
		String nickname = "호빵전사";
		doThrow(new BusinessException(ErrorCode.DUPLICATED_NICKNAME))
			.when(memberService).checkDuplicatedNickname(nickname);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/members/nickname")
				.with(csrf().asHeader())
				.param("nickname", nickname)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andDo(document("member/check-duplicated-nickname-fail",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					parameterWithName("nickname").description("닉네임")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("오류 코드"),
					fieldWithPath("errors").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@Test
	@DisplayName("닉네임 정보 수정 성공")
	void updateMemberSuccessTest() throws Exception {

		//given
		MemberModifyReq modifyReq = new MemberModifyReq("호빵전사");

		doNothing().when(memberService).changeNickname(1L, modifyReq);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/members/modify")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(modifyReq)))
			.andExpect(status().isNoContent())
			.andDo(document("member/modify-nickname-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("nickname").type(STRING).description("닉네임")
				)
			));
	}

	@Test
	@DisplayName("멤버 삭제 테스트 성공")
	void deleteMemberSuccessTest() throws Exception {

		//given
		doNothing().when(memberService).delete(any());

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/members")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNoContent())
			.andDo(document("member/delete-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				)
			));
	}

	@Test
	@DisplayName("프로필 이미지 수정 성공")
	void updateImageSuccessTest() throws Exception {

		//given
		MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "file".getBytes());
		FileDto fileDto = FileDto.builder()
			.originalFileName("originalName")
			.uploadFileName("uploadFileName")
			.uploadFilePath("uploadFilePath")
			.uploadFileUrl("uploadFileUrl")
			.build();
		ImagesRes imagesRes = new ImagesRes(fileDto.getUploadFileUrl());

		given(memberService.updateImage(any(), any()))
			.willReturn(imagesRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.fileUpload("/api/members/modify/image")
				.file(file)
				.with(updateRequest -> {
					updateRequest.setMethod("PATCH");
					return updateRequest;
				})
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(MULTIPART_FORM_DATA))
			.andExpect(status().isOk())
			.andDo(document("member/update-profile-image",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParts(
					partWithName("image").description("이미지")
				),
				responseFields(

					fieldWithPath("imageUrl").type(STRING).description("이미지 URL")
				)
			));
	}

	@Test
	@DisplayName("멤버 프로필 이미지 삭제 성공")
	void deleteImageSuccessTest() throws Exception {

		//given
		doNothing().when(memberService).deleteImage(any());

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/members/modify/image")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNoContent())
			.andDo(document("member/delete-profile-image",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				)
			));
	}
}
