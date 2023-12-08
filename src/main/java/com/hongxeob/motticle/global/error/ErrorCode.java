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
	JSON_PARSING_ERROR("C006", "유효하지 않은 JSON 형식입니다.");

	private final String code;
	private final String message;

}
