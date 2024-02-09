package com.hongxeob.motticle.article.application.dto.req;


import java.util.List;

public record SearchReq(
	Long memberId,
	List<Long> tagIds,
	List<String> tagNames,
	List<String> articleTypes,
	String sortOrder,
	Integer page,
	Integer size
) {

	public SearchReq {
        if (size == null || size <= 0) {
            size = DEFAULT_SIZE;
        }

        if (page == null || page < 0) {
            page = DEFAULT_PAGE;
        }
    }

    private static final int DEFAULT_SIZE = 10;
    private static final int DEFAULT_PAGE = 0;
}
