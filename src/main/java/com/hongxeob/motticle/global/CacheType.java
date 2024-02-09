package com.hongxeob.motticle.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheType {

	META_DATA("metaData", 1, 10000);

	private final String cacheName;
	private final int expiredAfterWrite;
	private final int maximumSize;
}
