package com.hongxeob.domain.tag;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TagRepositoryCustom {

	Slice<Tag> findAllByMemberId(Long memberId, Pageable pageable);

}
