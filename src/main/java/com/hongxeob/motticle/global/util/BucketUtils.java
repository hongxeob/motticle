package com.hongxeob.motticle.global.util;

import org.springframework.stereotype.Component;

import com.hongxeob.motticle.global.error.ErrorCode;
import com.hongxeob.motticle.global.error.exception.ExcessiveRequestException;

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
