package com.hongxeob.domain.auth;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByAccessToken(String accessToken);
}
