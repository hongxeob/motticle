package com.hongxeob.motticle.global.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Configuration
public class Bucket4jConfig {

	private static final int CAPACITY = 4;
	private static final int DURATION = 3;
	private static final int REFILL_TOKENS_COUNT = 4;

	@Bean
	public Bucket bucket() {
		Refill refill = Refill.intervally(
			REFILL_TOKENS_COUNT,
			Duration.ofSeconds(DURATION));

		Bandwidth limit = Bandwidth.classic(CAPACITY, refill);

		return Bucket.builder()
			.addLimit(limit)
			.build();
	}
}
