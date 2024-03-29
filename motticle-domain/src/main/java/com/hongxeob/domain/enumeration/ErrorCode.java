package com.hongxeob.domain.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	//서버
	INTERNAL_SERVER_ERROR("S001", "예기치 못한 오류가 발생했습니다."),
	UNABLE_TO_HANDLE_ERROR("S002", "처리할 수 없는 데이터입니다."),

	//공용
	INVALID_INPUT_VALUE("C001", "값이 비었거나, 잘못된 값을 입력하셨습니다."),
	UNAUTHORIZED_REQUEST("C002", "해당 요청을 수행할 권한이 없습니다."),
	INVALID_LIST_SORT_TYPE("C004", "유효하지 않은 정렬 조건입니다."),
	REQUIRE_QUERY_PARAM("C005", "URL에 추가적인 요청 조건이 필요합니다."),
	JSON_PARSING_ERROR("C006", "유효하지 않은 JSON 형식입니다."),
	INVALID_TOKEN("C007", "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN("C008", "만료된 토큰 입니다."),
	TOO_MANY_REQUESTS("C009", "단시간에 너무 많은 요청을 보냈습니다."),

	//이미지
	IMAGE_UPLOAD_FAILED("I001", "이미지 업로드에 실패 했습니다."),
	IMAGE_DELETE_FAILED("I002", "이미지 삭제에 실패 했습니다."),
	DEFAULT_IMAGE_ALREADY_SET("I003", "이미 기본 이미지는 삭제하실 수 없습니다"),
	INVALID_IMAGE_FORMAT("I004", "잘못된 이미지 포맷입니다."),

	//인증&인가
	REQUIRED_AUTHENTICATION("AUTH006", "토큰이 필요한 접근입니다."),

	//멤버
	NOT_FOUND_MEMBER("M001", "멤버를 찾을 수 없습니다."),
	GENDER_CANNOT_BE_EMPTY("M002", "성별은 비어있을 수 없습니다."),
	INVALID_GENDER_TYPE("M003", "잘못된 성별입니다."),
	DUPLICATED_NICKNAME("M004", "이미 존재하는 닉네임 입니다."),

	//아티클
	INVALID_ARTICLE_TYPE("A001", "잘못된 타입입니다."),
	UPLOAD_IMAGE_FILE("A002", "이미지 파일을 업로드 하세요"),
	NOT_FOUND_ARTICLE("A003", "아티클을 찾을 수 없습니다."),
	ARTICLE_OWNER_AND_REQUESTER_ARE_DIFFERENT("A004", "아티클 소유자와 요청자가 일치하지 않습니다."),
	ALREADY_REGISTERED_BY_TAG_IN_ARTICLE("A005", "이미 아티클에 등록된 태그입니다."),
	NOT_FOUND_REQUEST_TAG_IN_ARTICLE("A006", "해당 아티클에는 요청한 태그가 없습니다."),
	ARTICLE_IS_NOT_IMAGE_TYPE("T007", "해당 아티클은 이미지 타입이 아닙니다."),

	//태그
	NOT_FOUND_TAG("T001", "태그를 찾을 수 없습니다."),
	ALREADY_REGISTERED_BY_MEMBERS("T002", "이미 해당 회원이 등록한 태그입니다."),
	TAG_OWNER_AND_REQUESTER_ARE_DIFFERENT("T003", "태그 소유자와 요청자가 일치하지 않습니다."),

	//오픈 그래프
	LINK_CANNOT_BE_EMPTY("OG001", "링크 타입은 URL이 비어있을 수 없습니다."),
	LINK_TYPE_ONLY_USE("OG002", "링크 타입 아티클만 OpenGraph를 이용할 수 있습니다."),

	//스크랩
	ALREADY_SCRAPED_ARTICLE_BY_MEMBER("S001", "이미 스크랩된 아티클입니다."),
	NOT_FOUND_SCRAP_ARTICLE("S002", "해당 아티클로 스크랩된 내역이 없습니다."),
	ARTICLE_IS_PRIVATE("S003", "해당 아티클은 비공개처리 되었습니다."),

	//신고
	CANNOT_REPORT_YOUR_OWN_ARTICLE("R001", "본인 아티클은 신고할 수 없습니다."),
	ALREADY_REPORTED_ARTICLE_BY_SAME_MEMBER("R002", "이미 신고한 아티클입니다."),

	//알림
	INVALID_REDIS_MESSAGE_FORMAT("N001", "Redis 메시지 형식이 유효하지 않습니다."),
	SSE_STREAM_ERROR("N002", "SSE 스트림 연결 중 오류가 발생했습니다.");

	private final String code;
	private final String message;

}
