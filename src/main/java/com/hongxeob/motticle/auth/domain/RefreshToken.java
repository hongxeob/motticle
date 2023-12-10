package com.hongxeob.motticle.auth.domain;

import javax.persistence.Id;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@RedisHash(value = "jwtToken", timeToLive = 60 * 60 * 24 * 14)
@Builder
public class RefreshToken {

	@Id
	private String id;

	@Indexed
	private String accessToken;

	private String refreshToken;

	public void updateAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

}
