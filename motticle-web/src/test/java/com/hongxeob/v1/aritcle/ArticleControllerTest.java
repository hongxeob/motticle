package com.hongxeob.v1.aritcle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NULL;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import com.hongxeob.article.dto.req.ArticleAddReq;
import com.hongxeob.article.dto.req.ArticleModifyReq;
import com.hongxeob.article.dto.req.ArticleTaqReq;
import com.hongxeob.article.dto.res.ArticleInfoRes;
import com.hongxeob.article.dto.res.ArticleOgRes;
import com.hongxeob.article.dto.res.ArticlesOgRes;
import com.hongxeob.article.dto.res.OpenGraphResponse;
import com.hongxeob.domain.article.ArticleType;
import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.member.GenderType;
import com.hongxeob.domain.member.Role;
import com.hongxeob.member.dto.res.MemberInfoRes;
import com.hongxeob.tag.dto.res.TagRes;
import com.hongxeob.tag.dto.res.TagsRes;
import com.hongxeob.v1.common.ControllerTestSupport;

class ArticleControllerTest extends ControllerTestSupport {

	@Test
	@DisplayName("아티클 등록 성공 - 글 or 링크")
	void addArticleSuccessTest() throws Exception {

		//given
		List<Long> tagIds = List.of(1L, 2L);
		TagsRes tagsRes = new TagsRes(List.of(
			new TagRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now()),
			new TagRes(2L, "UI", 1L, LocalDateTime.now(), LocalDateTime.now())
		));

		ArticleAddReq articleAddReq = new ArticleAddReq("제목", ArticleType.TEXT.name(), "내용", "메모", true, tagIds);
		ArticleInfoRes articleInfoRes = new ArticleInfoRes(1L, articleAddReq.title(), articleAddReq.type(),
			articleAddReq.content(), articleAddReq.memo(), tagsRes, true, 1L, LocalDateTime.now(), LocalDateTime.now());

		MockMultipartFile request = new MockMultipartFile("articleAddReq", "articleAddReq",
			"application/json",
			objectMapper.writeValueAsString(articleAddReq).getBytes());

		given(articleService.register(any(), any(), any()))
			.willReturn(articleInfoRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.multipart("/api/articles")
				.file(request)
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.content(objectMapper.writeValueAsString(articleAddReq)))
			.andExpect(status().isOk())
			.andDo(document("article/add-success-link-or-text",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParts(
					partWithName("articleAddReq").description("아티클 등록 내용")
				),
				requestPartFields("articleAddReq",
					fieldWithPath("title").type(STRING).description("아티클 제목"),
					fieldWithPath("type").type(STRING).description("아티클 유형"),
					fieldWithPath("content").type(STRING).description("아티클 내용"),
					fieldWithPath("memo").type(STRING).description("아티클 메모"),
					fieldWithPath("isPublic").type(BOOLEAN).description("공개 여부"),
					fieldWithPath("tagIds").type(ARRAY).description("태그 ID 리스트")
				),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("아티클 ID"),
					fieldWithPath("title").type(STRING).description("아티클 제목"),
					fieldWithPath("type").type(STRING).description("아티클 유형"),
					fieldWithPath("content").type(STRING).description("아티클 내용"),
					fieldWithPath("memo").type(STRING).description("아티클 메모"),
					fieldWithPath("tagsRes.tagRes[].id").type(NUMBER).description("태그 ID"),
					fieldWithPath("tagsRes.tagRes[].name").type(STRING).description("태그 이름"),
					fieldWithPath("tagsRes.tagRes[].memberId").type(NUMBER).description("유저 ID"),
					fieldWithPath("tagsRes.tagRes[].createdAt").type(STRING).description("태그 생성일자"),
					fieldWithPath("tagsRes.tagRes[].updatedAt").type(STRING).description("태그 수정일자"),
					fieldWithPath("isPublic").type(BOOLEAN).description("공개 여부"),
					fieldWithPath("memberId").type(NUMBER).description("작성자 ID"),
					fieldWithPath("createdAt").type(STRING).description("생성일자"),
					fieldWithPath("updatedAt").type(STRING).description("수정일자")
				)
			));
	}

	@Test
	@DisplayName("아티클 등록 성공 - 이미지")
	void addArticleImageSuccessTest() throws Exception {

		//given
		List<Long> tagIds = List.of(1L, 2L);
		TagsRes tagsRes = new TagsRes(List.of(
			new TagRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now()),
			new TagRes(2L, "UI", 1L, LocalDateTime.now(), LocalDateTime.now())
		));

		ArticleAddReq articleAddReq = new ArticleAddReq("제목", ArticleType.TEXT.name(), null, "메모", true, tagIds);
		ArticleInfoRes articleInfoRes = new ArticleInfoRes(1L, articleAddReq.title(), articleAddReq.type(),
			"image-path", articleAddReq.memo(), tagsRes, true, 1L, LocalDateTime.now(), LocalDateTime.now());

		MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "file".getBytes());
		MockMultipartFile request = new MockMultipartFile("articleAddReq", "articleAddReq",
			"application/json",
			objectMapper.writeValueAsString(articleAddReq).getBytes());

		given(articleService.register(any(), any(), any()))
			.willReturn(articleInfoRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.multipart("/api/articles")
				.file(file)
				.file(request)
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.content(objectMapper.writeValueAsString(articleAddReq)))
			.andExpect(status().isOk())
			.andDo(document("article/add-success-image",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParts(
					partWithName("image").description("이미지"),
					partWithName("articleAddReq").description("아티클 등록 내용")
				),
				requestPartFields("articleAddReq",
					fieldWithPath("title").type(STRING).description("아티클 제목"),
					fieldWithPath("type").type(STRING).description("아티클 유형"),
					fieldWithPath("content").type(NULL).description("이미지 링크로 대체됨 (요구X)"),
					fieldWithPath("memo").type(STRING).description("아티클 메모"),
					fieldWithPath("isPublic").type(BOOLEAN).description("공개 여부"),
					fieldWithPath("tagIds").type(ARRAY).description("태그 ID 리스트")
				),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("아티클 ID"),
					fieldWithPath("title").type(STRING).description("아티클 제목"),
					fieldWithPath("type").type(STRING).description("아티클 유형"),
					fieldWithPath("content").type(STRING).description("아티클 이미지 경로"),
					fieldWithPath("memo").type(STRING).description("아티클 메모"),
					fieldWithPath("tagsRes.tagRes[].id").type(NUMBER).description("태그 ID"),
					fieldWithPath("tagsRes.tagRes[].name").type(STRING).description("태그 이름"),
					fieldWithPath("tagsRes.tagRes[].memberId").type(NUMBER).description("유저 ID"),
					fieldWithPath("tagsRes.tagRes[].createdAt").type(STRING).description("태그 생성일자"),
					fieldWithPath("tagsRes.tagRes[].updatedAt").type(STRING).description("태그 수정일자"),
					fieldWithPath("isPublic").type(BOOLEAN).description("공개 여부"),
					fieldWithPath("memberId").type(NUMBER).description("작성자 ID"),
					fieldWithPath("createdAt").type(STRING).description("생성일자"),
					fieldWithPath("updatedAt").type(STRING).description("수정일자")
				)
			));
	}

	@Test
	@DisplayName("멤버가 등록한 아티클 조회 성공 - 링크")
	void getLinkArticleByMemberSuccessTest() throws Exception {

		// given
		List<TagRes> tagResList = List.of(
			new TagRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now()),
			new TagRes(2L, "UI", 1L, LocalDateTime.now(), LocalDateTime.now())
		);

		OpenGraphResponse openGraphResponse = OpenGraphResponse.builder()
			.code(200)
			.description("링크 설명")
			.title("링크 제목")
			.url("링크")
			.image("이미지")
			.build();
		MemberInfoRes memberInfoRes = new MemberInfoRes(1L, "test@test", "닉네임", GenderType.FEMALE.name(), Role.USER.getKey(), "test/image.png");
		ArticleOgRes articleOgRes = new ArticleOgRes(1L, memberInfoRes, "제목1", "TEXT", "내용1", 0L, "메모1", new TagsRes(tagResList), true, 1L, openGraphResponse,
			LocalDateTime.now(), LocalDateTime.now());

		given(articleService.findByMemberId(any(), any()))
			.willReturn(articleOgRes);

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/articles/{id}", articleOgRes.id())
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("article/get-link-article-by-member-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("id").description("조회할 아티클 ID")
				),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("아티클 ID"),
					fieldWithPath("memberInfoRes.id").type(NUMBER).description("회원 ID"),
					fieldWithPath("memberInfoRes.email").type(STRING).description("회원 이메일"),
					fieldWithPath("memberInfoRes.nickname").type(STRING).description("회원 닉네임"),
					fieldWithPath("memberInfoRes.genderType").type(STRING).description("회원 성별"),
					fieldWithPath("memberInfoRes.role").type(STRING).description("회원 역할"),
					fieldWithPath("memberInfoRes.image").type(STRING).description("회원 이미지 경로"),
					fieldWithPath("title").type(STRING).description("아티클 제목"),
					fieldWithPath("type").type(STRING).description("아티클 유형"),
					fieldWithPath("content").type(STRING).description("아티클 내용 또는 이미지 경로"),
					fieldWithPath("scrapCount").type(NUMBER).description("스크랩 수"),
					fieldWithPath("memo").type(STRING).description("아티클 메모"),
					fieldWithPath("tagsRes.tagRes[].id").type(NUMBER).description("태그 ID"),
					fieldWithPath("tagsRes.tagRes[].name").type(STRING).description("태그 이름"),
					fieldWithPath("tagsRes.tagRes[].memberId").type(NUMBER).description("유저 ID"),
					fieldWithPath("tagsRes.tagRes[].createdAt").type(STRING).description("태그 생성일자"),
					fieldWithPath("tagsRes.tagRes[].updatedAt").type(STRING).description("태그 수정일자"),
					fieldWithPath("isPublic").type(BOOLEAN).description("공개 여부"),
					fieldWithPath("memberId").type(NUMBER).description("작성자 ID"),
					subsectionWithPath("openGraphResponse").type(OBJECT).description("링크 OpenGraph 응답 정보"),
					fieldWithPath("openGraphResponse.code").type(NUMBER).description("링크 파싱 여부 코드"),
					fieldWithPath("openGraphResponse.description").type(STRING).description("링크 설명"),
					fieldWithPath("openGraphResponse.title").type(STRING).description("링크 타이틀"),
					fieldWithPath("openGraphResponse.url").type(STRING).description("링크 URL"),
					fieldWithPath("openGraphResponse.image").type(STRING).description("링크 이미지 URL"),
					fieldWithPath("createdDatetime").type(STRING).description("생성일자"),
					fieldWithPath("updatedDatetime").type(STRING).description("수정일자")
				)
			));
	}

	@Test
	@DisplayName("멤버가 등록한 아티클 조회 성공 - 이미지 or 글")
	void getImageOrTextArticleByMemberSuccessTest() throws Exception {

		// given
		List<TagRes> tagResList = List.of(
			new TagRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now()),
			new TagRes(2L, "UI", 1L, LocalDateTime.now(), LocalDateTime.now())
		);

		OpenGraphResponse openGraphResponse = OpenGraphResponse.builder()
			.code(500)
			.description(null)
			.title(null)
			.url(null)
			.image(null)
			.build();

		MemberInfoRes memberInfoRes = new MemberInfoRes(1L, "test@test", "닉네임", GenderType.FEMALE.name(), Role.USER.getKey(), "test/image.png");
		ArticleOgRes articleOgRes = new ArticleOgRes(1L, memberInfoRes, "제목1", "TEXT", "내용1", 0L, "메모1", new TagsRes(tagResList), true, 1L, openGraphResponse,
			LocalDateTime.now(), LocalDateTime.now());

		given(articleService.findByMemberId(any(), any()))
			.willReturn(articleOgRes);

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/articles/{id}", articleOgRes.id())
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("article/get-image-or-text-article-by-member-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("id").description("조회할 아티클 ID")
				),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("아티클 ID"),
					fieldWithPath("memberInfoRes.id").type(NUMBER).description("회원 ID"),
					fieldWithPath("memberInfoRes.email").type(STRING).description("회원 이메일"),
					fieldWithPath("memberInfoRes.nickname").type(STRING).description("회원 닉네임"),
					fieldWithPath("memberInfoRes.genderType").type(STRING).description("회원 성별"),
					fieldWithPath("memberInfoRes.role").type(STRING).description("회원 역할"),
					fieldWithPath("memberInfoRes.image").type(STRING).description("회원 이미지 경로"),
					fieldWithPath("title").type(STRING).description("아티클 제목"),
					fieldWithPath("type").type(STRING).description("아티클 유형"),
					fieldWithPath("scrapCount").type(NUMBER).description("스크랩 수"),
					fieldWithPath("content").type(STRING).description("아티클 내용 또는 이미지 경로"),
					fieldWithPath("memo").type(STRING).description("아티클 메모"),
					fieldWithPath("tagsRes.tagRes[].id").type(NUMBER).description("태그 ID"),
					fieldWithPath("tagsRes.tagRes[].name").type(STRING).description("태그 이름"),
					fieldWithPath("tagsRes.tagRes[].memberId").type(NUMBER).description("유저 ID"),
					fieldWithPath("tagsRes.tagRes[].createdAt").type(STRING).description("태그 생성일자"),
					fieldWithPath("tagsRes.tagRes[].updatedAt").type(STRING).description("태그 수정일자"),
					fieldWithPath("isPublic").type(BOOLEAN).description("공개 여부"),
					fieldWithPath("memberId").type(NUMBER).description("작성자 ID"),
					subsectionWithPath("openGraphResponse").type(OBJECT).description("링크 OpenGraph 응답 정보"),
					fieldWithPath("openGraphResponse.code").type(NUMBER).description("링크 파싱 여부 코드"),
					fieldWithPath("openGraphResponse.description").type(NULL).description("링크 설명"),
					fieldWithPath("openGraphResponse.title").type(NULL).description("링크 제목"),
					fieldWithPath("openGraphResponse.url").type(NULL).description("링크 URL"),
					fieldWithPath("openGraphResponse.image").type(NULL).description("링크 이미지 URL"),
					fieldWithPath("createdDatetime").type(STRING).description("생성일자"),
					fieldWithPath("updatedDatetime").type(STRING).description("수정일자")
				)
			));
	}

	@Test
	@DisplayName("멤버가 등록한 아티클 조회 실패 - 등록된 아티클이 존재하지 않는다.")
	void getArticleByMemberFailTest() throws Exception {

		// given
		given(articleService.findByMemberId(any(), any()))
			.willThrow(new BusinessException(ErrorCode.NOT_FOUND_ARTICLE));

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/articles/{id}", 99L)
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andDo(document("article/get-fail-not-found-article-by-member",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("id").description("조회할 아티클 ID")
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
	@DisplayName("멤버가 등록한 모든 아티클 조회 성공")
	void getArticlesByMemberSuccessTest() throws Exception {

		List<TagRes> tagResList = List.of(
			new TagRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now()),
			new TagRes(2L, "UI", 1L, LocalDateTime.now(), LocalDateTime.now())
		);

		OpenGraphResponse openGraphResponse = OpenGraphResponse.builder()
			.code(200)
			.description("링크 설명")
			.title("링크 제목")
			.url("링크")
			.image("이미지")
			.build();

		int size = 5;
		int page = 0;
		MemberInfoRes memberInfoRes1 = new MemberInfoRes(1L, "test1@test", "닉네임1", GenderType.FEMALE.name(), Role.USER.getKey(), "test/image.png");
		MemberInfoRes memberInfoRes2 = new MemberInfoRes(2L, "test2@test", "닉네임2", GenderType.FEMALE.name(), Role.USER.getKey(), "test/image.png");

		List<ArticleOgRes> articleOgResList = List.of(
			new ArticleOgRes(1L, memberInfoRes1, "제목1", "TEXT", "내용1", 0L, "메모1", new TagsRes(tagResList), true, 1L, null,
				LocalDateTime.now(), LocalDateTime.now()),
			new ArticleOgRes(2L, memberInfoRes2, "제목2", "IMAGE", "이미지경로2", 0L, "메모2", new TagsRes(tagResList), true, 1L, openGraphResponse,
				LocalDateTime.now(), LocalDateTime.now())
		);

		ArticlesOgRes articlesOgRes = new ArticlesOgRes(articleOgResList, false);

		given(articleService.findAllByMemberId(any(), any()))
			.willReturn(articlesOgRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/articles")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.param("size", String.valueOf(size))
				.param("page", String.valueOf(1))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("article/get-all-by-member-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
				),
				requestParameters(
					parameterWithName("size").description("페이지 크기"),
					parameterWithName("page").description("페이지 번호")
				),
				responseFields(
					fieldWithPath("articleOgResList[].id").type(NUMBER).description("아티클 ID"),
					subsectionWithPath("articleOgResList[].memberInfoRes").type(OBJECT).optional().description("작성자 정보"),
					subsectionWithPath("articleOgResList[].memberInfoRes.id").type(NUMBER).description("회원 ID"),
					subsectionWithPath("articleOgResList[].memberInfoRes.email").type(STRING).description("회원 이메일"),
					subsectionWithPath("articleOgResList[].memberInfoRes.nickname").type(STRING).description("회원 닉네임"),
					subsectionWithPath("articleOgResList[].memberInfoRes.genderType").type(STRING).description("회원 성별"),
					subsectionWithPath("articleOgResList[].memberInfoRes.role").type(STRING).description("회원 역할"),
					subsectionWithPath("articleOgResList[].memberInfoRes.image").type(STRING).description("회원 이미지 경로"), fieldWithPath("articleOgResList[].title").type(STRING).description("아티클 제목"),
					fieldWithPath("articleOgResList[].type").type(STRING).description("아티클 유형"),
					fieldWithPath("articleOgResList[].content").type(STRING).description("아티클 내용"),
					fieldWithPath("articleOgResList[].memo").type(STRING).description("아티클 메모"),
					fieldWithPath("articleOgResList[].scrapCount").type(NUMBER).description("스크랩 수"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].id").type(NUMBER).description("태그 ID"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].name").type(STRING).description("태그 이름"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].memberId").type(NUMBER).description("유저 ID"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].createdAt").type(STRING).description("태그 생성일자"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].updatedAt").type(STRING).description("태그 수정일자"),
					fieldWithPath("articleOgResList[].isPublic").type(BOOLEAN).description("공개 여부"),
					fieldWithPath("articleOgResList[].memberId").type(NUMBER).description("작성자 ID"),
					fieldWithPath("articleOgResList[].openGraphResponse").type(OBJECT).optional().description("링크 OpenGraph 응답"),
					fieldWithPath("articleOgResList[].openGraphResponse.code").type(NUMBER).description("링크 파싱 여부 코드"),
					fieldWithPath("articleOgResList[].openGraphResponse.description").type(STRING).description("링크 설명"),
					fieldWithPath("articleOgResList[].openGraphResponse.title").type(STRING).description("링크 제목"),
					fieldWithPath("articleOgResList[].openGraphResponse.url").type(STRING).description("링크 URL"),
					fieldWithPath("articleOgResList[].openGraphResponse.image").type(STRING).description("링크 이미지"),
					fieldWithPath("articleOgResList[].createdDatetime").type(STRING).description("생성일자"),
					fieldWithPath("articleOgResList[].updatedDatetime").type(STRING).description("수정일자"),
					fieldWithPath("hasNext").type(BOOLEAN).description("다음 페이지 존재 여부")
				)
			));
	}

	@Test
	@DisplayName("아티클 수정 성공")
	void updateArticleSuccessTest() throws Exception {

		//given
		TagsRes tagsRes = new TagsRes(List.of(
			new TagRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now()),
			new TagRes(2L, "UI", 1L, LocalDateTime.now(), LocalDateTime.now())
		));
		ArticleModifyReq modifyReq = new ArticleModifyReq("수정 제목", "수정 내용", "수정 메모", true);
		ArticleInfoRes articleInfoRes = new ArticleInfoRes(1L, modifyReq.title(), ArticleType.TEXT.name(),
			modifyReq.content(), modifyReq.memo(), tagsRes, true, 1L, LocalDateTime.now(), LocalDateTime.now());

		given(articleService.modify(any(), any(), any()))
			.willReturn(articleInfoRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/articles/{id}", 1L)
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.content(objectMapper.writeValueAsString(modifyReq))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("article/update-article-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("id").description("수정할 아티클 ID")
				),
				requestFields(
					fieldWithPath("title").type(STRING).description("수정할 제목"),
					fieldWithPath("content").type(STRING).description("수정할 내용"),
					fieldWithPath("memo").type(STRING).description("수정할 메모"),
					fieldWithPath("isPublic").type(BOOLEAN).description("공개여부 수정")
				),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("아티클 ID"),
					fieldWithPath("title").type(STRING).description("아티클 제목"),
					fieldWithPath("type").type(STRING).description("아티클 유형"),
					fieldWithPath("content").type(STRING).description("아티클 내용"),
					fieldWithPath("memo").type(STRING).description("아티클 메모"),
					fieldWithPath("tagsRes.tagRes[].id").type(NUMBER).description("태그 ID"),
					fieldWithPath("tagsRes.tagRes[].name").type(STRING).description("태그 이름"),
					fieldWithPath("tagsRes.tagRes[].memberId").type(NUMBER).description("유저 ID"),
					fieldWithPath("tagsRes.tagRes[].createdAt").type(STRING).description("태그 생성일자"),
					fieldWithPath("tagsRes.tagRes[].updatedAt").type(STRING).description("태그 수정일자"),
					fieldWithPath("isPublic").type(BOOLEAN).description("공개 여부"),
					fieldWithPath("memberId").type(NUMBER).description("작성자 ID"),
					fieldWithPath("createdAt").type(STRING).description("생성일자"),
					fieldWithPath("updatedAt").type(STRING).description("수정일자")
				)
			));
	}

	@Test
	@DisplayName("아티클에 태그 추가 성공")
	void addTagToArticleSuccessTest() throws Exception {

		//given
		Long articleId = 1L;
		ArticleTaqReq articleTaqReq = new ArticleTaqReq(2L);

		TagsRes tagsRes = new TagsRes(List.of(
			new TagRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now()),
			new TagRes(2L, "UI", 1L, LocalDateTime.now(), LocalDateTime.now())
		));
		ArticleInfoRes articleInfoRes = new ArticleInfoRes(1L, "제목", ArticleType.TEXT.name(),
			"내용", "메모", tagsRes, true, 1L, LocalDateTime.now(), LocalDateTime.now());

		given(articleService.tagArticle(any(), any(), any()))
			.willReturn(articleInfoRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/articles/{id}/tags", articleId)
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(articleTaqReq)))
			.andExpect(status().isOk())
			.andDo(document("article/add-tag-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("id").description("태그 추가할 아티클 ID")
				),
				requestFields(
					fieldWithPath("tagId").description("추가할 태그 ID")
				),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("아티클 ID"),
					fieldWithPath("title").type(STRING).description("아티클 제목"),
					fieldWithPath("type").type(STRING).description("아티클 유형"),
					fieldWithPath("content").type(STRING).description("아티클 내용"),
					fieldWithPath("memo").type(STRING).description("아티클 메모"),
					fieldWithPath("tagsRes.tagRes[].id").type(NUMBER).description("태그 ID"),
					fieldWithPath("tagsRes.tagRes[].name").type(STRING).description("태그 이름"),
					fieldWithPath("tagsRes.tagRes[].memberId").type(NUMBER).description("유저 ID"),
					fieldWithPath("tagsRes.tagRes[].createdAt").type(STRING).description("태그 생성일자"),
					fieldWithPath("tagsRes.tagRes[].updatedAt").type(STRING).description("태그 수정일자"),
					fieldWithPath("isPublic").type(BOOLEAN).description("공개 여부"),
					fieldWithPath("memberId").type(NUMBER).description("작성자 ID"),
					fieldWithPath("createdAt").type(STRING).description("생성일자"),
					fieldWithPath("updatedAt").type(STRING).description("수정일자")
				)
			));
	}

	@Test
	@DisplayName("아티클에 태그 추가 실패 - 아티클에 이미 존재하는 태그")
	void addTagToArticleFailTest_already_registered() throws Exception {

		//given
		Long articleId = 1L;
		ArticleTaqReq articleTaqReq = new ArticleTaqReq(2L);

		given(articleService.tagArticle(any(), any(), any()))
			.willThrow(new BusinessException(ErrorCode.ALREADY_REGISTERED_BY_TAG_IN_ARTICLE));

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/articles/{id}/tags", articleId)
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(articleTaqReq)))
			.andExpect(status().isBadRequest())
			.andDo(document("article/add-tag-fail-already-registered",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("id").description("태그 등록할 아티클 ID")
				),
				requestFields(
					fieldWithPath("tagId").description("추가할 태그 ID")
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
	@DisplayName("아티클에 태그 해제 성공")
	void deleteTagFromArticleSuccessTest() throws Exception {

		//given
		Long tagId = 2L;
		Long articleId = 1L;

		doNothing().when(articleService)
			.unTagArticle(any(), any(), any());

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/articles/{id}/un-tags", articleId)
				.param("tag", String.valueOf(tagId))
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNoContent())
			.andDo(document("article/remove-tag-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("id").description("태그 삭제할 아티클 ID")
				),
				requestParameters(
					parameterWithName("tag").description("삭제할 태그 ID")
				)
			));
	}

	@Test
	@DisplayName("아티클에 태그 해제 실패 - 해당 아티클에 존재하지 않는 태그")
	void deleteTagFromArticleFailTest_notFound() throws Exception {

		//given
		Long tagId = 2L;
		Long articleId = 1L;

		doThrow(new BusinessException(ErrorCode.NOT_FOUND_REQUEST_TAG_IN_ARTICLE))
			.when(articleService).unTagArticle(any(), any(), any());

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/articles/{id}/un-tags", articleId)
				.param("tag", String.valueOf(tagId))
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andDo(document("article/remove-tag-fail-not-found-tag",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("id").description("태그 삭제할 아티클 ID")
				),
				requestParameters(
					parameterWithName("tag").description("삭제할 태그 ID")
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
	@DisplayName("아티클 삭제 성공")
	void deleteArticleSuccessTest() throws Exception {

		//given
		doNothing().when(articleService).unTagArticleByArticle(any(), any());
		doNothing().when(scrapService).removeAllScrapsForArticle(any());
		doNothing().when(articleService).remove(any(), any());

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/articles/{id}", 1L)
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNoContent())
			.andDo(document("article/remove-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("id").description("삭제할 아티클 ID")
				)
			));
	}

	@Test
	@DisplayName("아티클 삭제 실패 - 소유자와 요청자가 일치하지 않음")
	void deleteArticleFailTest_notMatch() throws Exception {

		//given
		doThrow(new BusinessException(ErrorCode.TAG_OWNER_AND_REQUESTER_ARE_DIFFERENT))
			.when(articleService).unTagArticleByArticle(any(), any());
		doNothing().when(articleService).remove(any(), any());

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/articles/{id}", 1L)
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andDo(document("article/remove-fail-not-match-owner-and-requester",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("id").description("삭제할 아티클 ID")
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
	@DisplayName("필터를 통한 내 아티클 검색 성공")
	void getArticlesByMemberAndConditionSuccessTest() throws Exception {

		//given
		List<Long> tagIds = List.of(1L, 2L);
		TagsRes tagsRes = new TagsRes(List.of(
			new TagRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now()),
			new TagRes(2L, "UI", 1L, LocalDateTime.now(), LocalDateTime.now())
		));

		OpenGraphResponse openGraphResponse = OpenGraphResponse.builder()
			.code(200)
			.description("링크 설명")
			.title("링크 제목")
			.url("링크")
			.image("이미지")
			.build();

		MemberInfoRes memberInfoRes1 = new MemberInfoRes(1L, "test1@test", "닉네임1", GenderType.FEMALE.name(), Role.USER.getKey(), "test/image.png");
		MemberInfoRes memberInfoRes2 = new MemberInfoRes(2L, "test2@test", "닉네임2", GenderType.FEMALE.name(), Role.USER.getKey(), "test/image.png");

		List<ArticleOgRes> articleOgResList = List.of(
			new ArticleOgRes(1L, memberInfoRes1, "제목1", "TEXT", "내용1", 0L,
				"메모1", tagsRes, true, 1L, null,
				LocalDateTime.now(), LocalDateTime.now()),
			new ArticleOgRes(2L, memberInfoRes2, "제목2", "IMAGE", "이미지경로2", 0L,
				"메모2", tagsRes, true, 1L, openGraphResponse,
				LocalDateTime.now(), LocalDateTime.now())
		);
		ArticlesOgRes articlesOgRes = new ArticlesOgRes(articleOgResList, false);

		given(articleService.findAllByMemberAndCondition(any(), any(), any()))
			.willReturn(articlesOgRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/articles/search")
				.with(csrf().asHeader())
				.header(HttpHeaders.AUTHORIZATION, "{AccessToken}")
				.param("tagIds", "1", "2")
				.param("articleTypes", "TEXT", "IMAGE")
				.param("keyword", "제목")
				.param("sortOrder", "oldest")
				.param("size", String.valueOf(5))
				.param("page", String.valueOf(1))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("article/search-all-by-condition",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
				),
				requestParameters(
					parameterWithName("tagIds").description("필터링할 태그ID 목록"),
					parameterWithName("articleTypes").description("필터링할 아티클 타입 목록"),
					parameterWithName("keyword").description("검색할 키워드"),
					parameterWithName("sortOrder").description("정렬 방법"),
					parameterWithName("size").description("페이지 크기"),
					parameterWithName("page").description("페이지 번호")
				),
				responseFields(
					fieldWithPath("articleOgResList[].id").type(NUMBER).description("아티클 ID"),
					subsectionWithPath("articleOgResList[].memberInfoRes").type(OBJECT).optional().description("작성자 정보"),
					subsectionWithPath("articleOgResList[].memberInfoRes.id").type(NUMBER).description("회원 ID"),
					subsectionWithPath("articleOgResList[].memberInfoRes.email").type(STRING).description("회원 이메일"),
					subsectionWithPath("articleOgResList[].memberInfoRes.nickname").type(STRING).description("회원 닉네임"),
					subsectionWithPath("articleOgResList[].memberInfoRes.genderType").type(STRING).description("회원 성별"),
					subsectionWithPath("articleOgResList[].memberInfoRes.role").type(STRING).description("회원 역할"),
					subsectionWithPath("articleOgResList[].memberInfoRes.image").type(STRING).description("회원 이미지 경로"),
					fieldWithPath("articleOgResList[].title").type(STRING).description("아티클 제목"),
					fieldWithPath("articleOgResList[].type").type(STRING).description("아티클 유형"),
					fieldWithPath("articleOgResList[].content").type(STRING).description("아티클 내용"),
					fieldWithPath("articleOgResList[].memo").type(STRING).description("아티클 메모"),
					fieldWithPath("articleOgResList[].scrapCount").type(NUMBER).description("스크랩 수"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].id").type(NUMBER).description("태그 ID"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].name").type(STRING).description("태그 이름"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].memberId").type(NUMBER).description("유저 ID"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].createdAt").type(STRING).description("태그 생성일자"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].updatedAt").type(STRING).description("태그 수정일자"),
					fieldWithPath("articleOgResList[].isPublic").type(BOOLEAN).description("공개 여부"),
					fieldWithPath("articleOgResList[].memberId").type(NUMBER).description("작성자 ID"),
					fieldWithPath("articleOgResList[].openGraphResponse").type(OBJECT).optional().description("링크 OpenGraph 응답"),
					fieldWithPath("articleOgResList[].openGraphResponse.code").type(NUMBER).description("링크 파싱 여부 코드"),
					fieldWithPath("articleOgResList[].openGraphResponse.description").type(STRING).description("링크 설명"),
					fieldWithPath("articleOgResList[].openGraphResponse.title").type(STRING).description("링크 제목"),
					fieldWithPath("articleOgResList[].openGraphResponse.url").type(STRING).description("링크 URL"),
					fieldWithPath("articleOgResList[].openGraphResponse.image").type(STRING).description("링크 이미지"),
					fieldWithPath("articleOgResList[].createdDatetime").type(STRING).description("생성일자"),
					fieldWithPath("articleOgResList[].updatedDatetime").type(STRING).description("수정일자"),
					fieldWithPath("hasNext").type(BOOLEAN).description("다음 페이지 존재 여부")
				)
			));
	}

	@Test
	@DisplayName("필터를 통한 모든 아티클 조회 성공")
	void getArticlesByConditionSuccessTest() throws Exception {

		//given
		List<Long> tagIds = List.of(1L, 2L);
		TagsRes tagsRes = new TagsRes(List.of(
			new TagRes(1L, "IT", 1L, LocalDateTime.now(), LocalDateTime.now()),
			new TagRes(2L, "UI", 1L, LocalDateTime.now(), LocalDateTime.now())
		));

		OpenGraphResponse openGraphResponse = OpenGraphResponse.builder()
			.code(200)
			.description("링크 설명")
			.title("링크 제목")
			.url("링크")
			.image("이미지")
			.build();

		MemberInfoRes memberInfoRes1 = new MemberInfoRes(1L, "test1@test", "닉네임1", GenderType.FEMALE.name(), Role.USER.getKey(), "test/image.png");
		MemberInfoRes memberInfoRes2 = new MemberInfoRes(2L, "test2@test", "닉네임2", GenderType.FEMALE.name(), Role.USER.getKey(), "test/image.png");

		List<ArticleOgRes> articleOgResList = List.of(
			new ArticleOgRes(1L, memberInfoRes1, "제목1", "TEXT", "내용1", 0L,
				"메모1", tagsRes, true, 1L, null,
				LocalDateTime.now(), LocalDateTime.now()),
			new ArticleOgRes(2L, memberInfoRes2, "제목2", "IMAGE", "이미지경로2", 0L,
				"메모2", tagsRes, true, 1L, openGraphResponse,
				LocalDateTime.now(), LocalDateTime.now())
		);
		ArticlesOgRes articlesOgRes = new ArticlesOgRes(articleOgResList, false);

		given(articleService.findAllByCondition(any(), any(), any()))
			.willReturn(articlesOgRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/articles/explore")
				.with(csrf().asHeader())
				.param("tagIds", "1", "2")
				.param("articleTypes", "TEXT", "IMAGE")
				.param("keyword", "제목")
				.param("sortOrder", "oldest")
				.param("size", String.valueOf(5))
				.param("page", String.valueOf(1))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("article/explore-get-all-by-condition",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					parameterWithName("tagIds").description("필터링할 태그ID 목록"),
					parameterWithName("articleTypes").description("필터링할 아티클 타입 목록"),
					parameterWithName("keyword").description("검색할 키워드"),
					parameterWithName("sortOrder").description("정렬 방법"),
					parameterWithName("size").description("페이지 크기"),
					parameterWithName("page").description("페이지 번호")
				),
				responseFields(
					fieldWithPath("articleOgResList[].id").type(NUMBER).description("아티클 ID"),
					subsectionWithPath("articleOgResList[].memberInfoRes").type(OBJECT).optional().description("작성자 정보"),
					subsectionWithPath("articleOgResList[].memberInfoRes.id").type(NUMBER).description("회원 ID"),
					subsectionWithPath("articleOgResList[].memberInfoRes.email").type(STRING).description("회원 이메일"),
					subsectionWithPath("articleOgResList[].memberInfoRes.nickname").type(STRING).description("회원 닉네임"),
					subsectionWithPath("articleOgResList[].memberInfoRes.genderType").type(STRING).description("회원 성별"),
					subsectionWithPath("articleOgResList[].memberInfoRes.role").type(STRING).description("회원 역할"),
					subsectionWithPath("articleOgResList[].memberInfoRes.image").type(STRING).description("회원 이미지 경로"), fieldWithPath("articleOgResList[].title").type(STRING).description("아티클 제목"),
					fieldWithPath("articleOgResList[].type").type(STRING).description("아티클 유형"),
					fieldWithPath("articleOgResList[].content").type(STRING).description("아티클 내용"),
					fieldWithPath("articleOgResList[].memo").type(STRING).description("아티클 메모"),
					fieldWithPath("articleOgResList[].scrapCount").type(NUMBER).description("스크랩 수"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].id").type(NUMBER).description("태그 ID"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].name").type(STRING).description("태그 이름"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].memberId").type(NUMBER).description("유저 ID"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].createdAt").type(STRING).description("태그 생성일자"),
					fieldWithPath("articleOgResList[].tagsRes.tagRes[].updatedAt").type(STRING).description("태그 수정일자"),
					fieldWithPath("articleOgResList[].isPublic").type(BOOLEAN).description("공개 여부"),
					fieldWithPath("articleOgResList[].memberId").type(NUMBER).description("작성자 ID"),
					fieldWithPath("articleOgResList[].openGraphResponse").type(OBJECT).optional().description("링크 OpenGraph 응답"),
					fieldWithPath("articleOgResList[].openGraphResponse.code").type(NUMBER).description("링크 파싱 여부 코드"),
					fieldWithPath("articleOgResList[].openGraphResponse.description").type(STRING).description("링크 설명"),
					fieldWithPath("articleOgResList[].openGraphResponse.title").type(STRING).description("링크 제목"),
					fieldWithPath("articleOgResList[].openGraphResponse.url").type(STRING).description("링크 URL"),
					fieldWithPath("articleOgResList[].openGraphResponse.image").type(STRING).description("링크 이미지"),
					fieldWithPath("articleOgResList[].createdDatetime").type(STRING).description("생성일자"),
					fieldWithPath("articleOgResList[].updatedDatetime").type(STRING).description("수정일자"),
					fieldWithPath("hasNext").type(BOOLEAN).description("다음 페이지 존재 여부")
				)
			));
	}
}
