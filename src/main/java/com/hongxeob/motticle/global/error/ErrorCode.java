package com.hongxeob.motticle.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	//서버
	INTERNAL_SERVER_ERROR("S001", "예기치 못한 오류가 발생했습니다."),
	UNABLE_TO_HANDLE_ERROR("S002", "처리할 수 없는 데이터입니다."),

	//공용
	INVALID_INPUT_VALUE("C001", "잘못된 값을 입력하셨습니다."),
	UNAUTHORIZED_REQUEST("C002", "해당 요청을 수행할 권한이 없습니다."),
	INVALID_LIST_SORT_TYPE("C004", "유효하지 않은 정렬 조건입니다."),
	REQUIRE_QUERY_PARAM("C005", "URL에 추가적인 요청 조건이 필요합니다."),
	JSON_PARSING_ERROR("C006", "유효하지 않은 JSON 형식입니다."),
	INVALID_TOKEN("C007", "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN("C008", "만료된 토큰 입니다."),

	//이미지
	IMAGE_UPLOAD_FAILED("I001", "이미지 업로드에 실패 했습니다."),
	IMAGE_DELETE_FAILED("I002", "이미지 삭제에 실패 했습니다."),

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

	//태그
	NOT_FOUND_TAG("T001", "태그를 찾을 수 없습니다."),
	ALREADY_REGISTERED_BY_MEMBERS("T002", "이미 해당 회원이 등록한 태그입니다."),
	TAG_OWNER_AND_REQUESTER_ARE_DIFFERENT("T003", "태그 소유자와 요청자가 일치하지 않습니다."),
	;

	private final String code;
	private final String message;

}
