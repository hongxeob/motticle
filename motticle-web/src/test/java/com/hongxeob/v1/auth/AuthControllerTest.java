package com.hongxeob.v1.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import com.hongxeob.auth.dto.TokenResponse;
import com.hongxeob.v1.common.ControllerTestSupport;

class AuthControllerTest extends ControllerTestSupport {

	@Test
	@DisplayName("로그아웃 성공")
	void logoutSuccessTest() throws Exception {

		//given
		doNothing().when(refreshTokenService).removeRefreshToken(any());

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/auth/logout")
				.header("Authorization", "{AccessToken}"))
			.andExpect(status().isNoContent())
			.andDo(document("auth/logout",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				)
			));
	}

	@Test
	@DisplayName("토큰 재발급 성공")
	void reissueTokenSuccessTest() throws Exception {

		//given
		String accessToken = "accessToken";
		TokenResponse tokenResponse = new TokenResponse(accessToken);

		given(refreshTokenService.reissueToken(any()))
			.willReturn(tokenResponse);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/auth/reissue")
				.header("Authorization", "{AccessToken}"))
			.andExpect(status().isOk())
			.andDo(document("auth/reissue-token",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Authorization").description("Access Token")
					),
					responseFields(
						fieldWithPath("accessToken").type(STRING).description("재발급 accessToken")
					)
				)
			);
	}
}
