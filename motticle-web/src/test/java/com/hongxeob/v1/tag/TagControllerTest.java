package com.hongxeob.v1.tag;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.tag.dto.req.TagReq;
import com.hongxeob.tag.dto.res.TagRes;
import com.hongxeob.tag.dto.res.TagSliceRes;
import com.hongxeob.tag.dto.res.TagsSliceRes;
import com.hongxeob.v1.common.ControllerTestSupport;

class TagControllerTest extends ControllerTestSupport {

	@Test
	@DisplayName("태그 등록 성공")
	void addTagSuccessTest() throws Exception {

		//given
		TagReq tagReq = new TagReq("IT");
		TagRes tagRes = new TagRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now());

		given(tagService.register(any(), any()))
			.willReturn(tagRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/tags")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tagReq)))
			.andExpect(status().isOk())
			.andDo(document("tag/add-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("name").type(STRING).description("태그 이름")
				),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("태그 ID"),
					fieldWithPath("name").type(STRING).description("태그 이름"),
					fieldWithPath("memberId").type(NUMBER).description("유저 ID"),
					fieldWithPath("createdAt").type(STRING).description("생성일자"),
					fieldWithPath("updatedAt").type(STRING).description("수정일자")
				)
			));
	}

	@Test
	@DisplayName("태그 등록 실패 - 이미 존재하는 태그")
	void addTagFailTest_alreadyRegistered() throws Exception {

		//given
		TagReq tagReq = new TagReq("IT");

		given(tagService.register(any(), any()))
			.willThrow(new BusinessException(ErrorCode.ALREADY_REGISTERED_BY_MEMBERS));

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/tags")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tagReq)))
			.andExpect(status().isBadRequest())
			.andDo(document("tag/add-fail-already-registered",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("name").type(STRING).description("태그 이름")
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
	@DisplayName("해당 유저가 등록한 모든 태그 조회 성공")
	void getAllTagByMemberSuccessTest() throws Exception {

		//given
		TagsSliceRes tagsSliceRes = new TagsSliceRes(List.of(
			new TagSliceRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now()),
			new TagSliceRes(2L, "UI", 1L, LocalDateTime.now(), LocalDateTime.now())
		), false);

		given(tagService.findAllByMemberId(any(), any()))
			.willReturn(tagsSliceRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/tags")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("tag/get-all-by-member",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				responseFields(
					fieldWithPath("tagSliceRes").type(ARRAY).description("태그 목록"),
					fieldWithPath("tagSliceRes[].id").type(NUMBER).description("태그 ID"),
					fieldWithPath("tagSliceRes[].name").type(STRING).description("태그 이름"),
					fieldWithPath("tagSliceRes[].memberId").type(NUMBER).description("유저 ID"),
					fieldWithPath("tagSliceRes[].createdAt").type(STRING).description("생성일자"),
					fieldWithPath("tagSliceRes[].updatedAt").type(STRING).description("수정일자"),
					fieldWithPath("hasNext").type(BOOLEAN).description("다음 페이지 여부")
				)
			));
	}

	@Test
	@DisplayName("태그 삭제 성공")
	void deleteTagSuccessTest() throws Exception {

		//given
		Long tagId = 1L;
		Long memberId = 1L;

		// Mock the service methods
		doNothing().when(tagService).delete(anyLong(), anyLong());
		doNothing().when(articleService).unTagArticleByTag(anyLong(), anyLong());

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/tags/{id}", tagId)
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNoContent())
			.andDo(document("tag/delete-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("id").description("삭제할 태그의 ID")
				)
			));


	}
}
