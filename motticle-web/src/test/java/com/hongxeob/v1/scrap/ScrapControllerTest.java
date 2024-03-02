package com.hongxeob.v1.scrap;

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
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import com.hongxeob.article.dto.req.ArticleAddReq;
import com.hongxeob.article.dto.res.ArticleInfoRes;
import com.hongxeob.domain.article.ArticleType;
import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.scrap.dto.req.ScrapReq;
import com.hongxeob.scrap.dto.res.ScrapRes;
import com.hongxeob.tag.dto.res.TagRes;
import com.hongxeob.tag.dto.res.TagsRes;
import com.hongxeob.v1.common.ControllerTestSupport;

class ScrapControllerTest extends ControllerTestSupport {

	@Test
	@DisplayName("스크랩 등록 성공")
	void scrapSuccessTest() throws Exception {

		//given
		List<Long> tagIds = List.of(1L, 2L);
		TagsRes tagsRes = new TagsRes(List.of(
			new TagRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now()),
			new TagRes(2L, "UI", 1L, LocalDateTime.now(), LocalDateTime.now())
		));
		ArticleAddReq articleAddReq = new ArticleAddReq("제목", ArticleType.TEXT.name(), "내용", "메모", true, tagIds);

		ArticleInfoRes articleInfoRes = new ArticleInfoRes(1L, articleAddReq.title(), articleAddReq.type(),
			articleAddReq.content(), articleAddReq.memo(), tagsRes, true, 1L, LocalDateTime.now(), LocalDateTime.now());
		ScrapReq scrapReq = new ScrapReq(1L);
		ScrapRes scrapRes = new ScrapRes(1L, articleInfoRes);

		given(scrapService.scrap(any(), any()))
			.willReturn(scrapRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/scraps")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(scrapReq)))
			.andExpect(status().isOk())
			.andDo(document("scrap/add-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("articleId").type(NUMBER).description("아티클 ID")
				),
				responseFields(
					fieldWithPath("memberId").type(NUMBER).description("스크랩 요청자ID"),
					fieldWithPath("articleInfoRes.id").type(NUMBER).description("아티클 ID"),
					fieldWithPath("articleInfoRes.title").type(STRING).description("아티클 제목"),
					fieldWithPath("articleInfoRes.type").type(STRING).description("아티클 타입"),
					fieldWithPath("articleInfoRes.content").type(STRING).description("아티클 내용"),
					fieldWithPath("articleInfoRes.memo").type(STRING).description("아티클 메모"),
					fieldWithPath("articleInfoRes.tagsRes").type(OBJECT).description("아티클 태그 정보"),
					fieldWithPath("articleInfoRes.isPublic").type(BOOLEAN).description("아티클 공개 여부"),
					fieldWithPath("articleInfoRes.memberId").type(NUMBER).description("아티클 작성자 ID"),
					fieldWithPath("articleInfoRes.createdAt").type(STRING).description("아티클 생성일시"),
					fieldWithPath("articleInfoRes.updatedAt").type(STRING).description("아티클 수정일시"),
					fieldWithPath("articleInfoRes.tagsRes.tagRes[].id").type(NUMBER).description("태그 ID"),
					fieldWithPath("articleInfoRes.tagsRes.tagRes[].name").type(STRING).description("태그 이름"),
					fieldWithPath("articleInfoRes.tagsRes.tagRes[].memberId").type(NUMBER).description("태그 작성자 ID"),
					fieldWithPath("articleInfoRes.tagsRes.tagRes[].createdAt").type(STRING).description("태그 생성일시"),
					fieldWithPath("articleInfoRes.tagsRes.tagRes[].updatedAt").type(STRING).description("태그 수정일시")
				)
			));
	}

	@Test
	@DisplayName("스크랩 실패 - 이미 등록된 태그")
	void scrapFailTest_alreadyScrapped() throws Exception {

		//given
		ScrapReq scrapReq = new ScrapReq(1L);

		given(scrapService.scrap(any(), any()))
			.willThrow(new BusinessException(ErrorCode.ALREADY_SCRAPED_ARTICLE_BY_MEMBER));

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/scraps")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(scrapReq)))
			.andExpect(status().isBadRequest())
			.andDo(document("scrap/fail-already-scrapped",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("articleId").type(NUMBER).description("스크랩할 아티클 ID")
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
