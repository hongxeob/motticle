package com.hongxeob.common.util;

import org.springframework.stereotype.Component;

import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.error.exception.ExcessiveRequestException;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class BucketUtils {

	private static final int CONSUME_BUCKET_COUNT = 1;
	private final Bucket bucket;

	public void checkRequestBucketCount() {
		if (bucket.tryConsume(CONSUME_BUCKET_COUNT)) {
			return;
		}

		log.warn("POST/PATCH:CREATE/UPDATE:TOO_MANY_REQUESTS");
		throw new ExcessiveRequestException(ErrorCode.TOO_MANY_REQUESTS);
	}
}
