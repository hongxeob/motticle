package com.hongxeob.v1.report;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.report.dto.req.ReportReq;
import com.hongxeob.report.dto.res.ReportRes;
import com.hongxeob.v1.common.ControllerTestSupport;

class ReportControllerTest extends ControllerTestSupport {

	@Test
	@DisplayName("아티클 신고 성공")
	void reportSuccessTest() throws Exception {

		//given
		ReportReq req = new ReportReq(1L, "욕설이 너무 많아요");
		ReportRes res = new ReportRes(1L, 1L, 1L, req.content(), LocalDateTime.now());

		given(reportService.reportArticle(any(), any()))
			.willReturn(res);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/reports")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andExpect(status().isOk())
			.andDo(document("report/report-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("articleId").type(NUMBER).description("신고할 아티클 ID"),
					fieldWithPath("content").type(STRING).description("신고 내용")
				),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("신고 ID"),
					fieldWithPath("articleId").type(NUMBER).description("신고 아티클 ID"),
					fieldWithPath("reporterId").type(NUMBER).description("신고자 ID"),
					fieldWithPath("content").type(STRING).description("신고 내용"),
					fieldWithPath("createdAt").type(STRING).description("신고 시간")
				)
			));
	}

	@Test
	@DisplayName("아티클 신고 실패 - 이미 내가 신고한 아티클")
	void reportFailTest_alreadyReport() throws Exception {

		//given
		ReportReq req = new ReportReq(1L, "욕설이 너무 많아요");

		given(reportService.reportArticle(any(), any()))
			.willThrow(new BusinessException(ErrorCode.ALREADY_REPORTED_ARTICLE_BY_SAME_MEMBER));

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/reports")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andExpect(status().isBadRequest())
			.andDo(document("report/report-fail-already-reported",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("articleId").type(NUMBER).description("신고할 아티클 ID"),
					fieldWithPath("content").type(STRING).description("신고 내용")
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
	@DisplayName("아티클 신고 실패 - 본인 아티클 본인이 신고")
	void reportFailTest_reportOwnArticle() throws Exception {

		//given
		ReportReq req = new ReportReq(1L, "욕설이 너무 많아요");

		given(reportService.reportArticle(any(), any()))
			.willThrow(new BusinessException(ErrorCode.CANNOT_REPORT_YOUR_OWN_ARTICLE));

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/reports")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andExpect(status().isBadRequest())
			.andDo(document("report/report-fail-own-article",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("articleId").type(NUMBER).description("신고할 아티클 ID"),
					fieldWithPath("content").type(STRING).description("신고 내용")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("오류 코드"),
					fieldWithPath("errors").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

}
