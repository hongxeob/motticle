package com.hongxeob.motticle.global.config;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.hongxeob.motticle.global.CacheType;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		List<CaffeineCache> caches = Arrays.stream(CacheType.values())
			.map(cache -> new CaffeineCache(cache.getCacheName(), Caffeine.newBuilder().recordStats()
					.expireAfterWrite(cache.getExpiredAfterWrite(), TimeUnit.HOURS)
					.maximumSize(cache.getMaximumSize())
					.build()
				)
			)
			.toList();

		SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
		simpleCacheManager.setCaches(caches);
		return simpleCacheManager;
	}
}
