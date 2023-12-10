package com.hongxeob.motticle.auth.domain;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByAccessToken(String accessToken);
}
