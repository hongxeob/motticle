package com.hongxeob.motticle.tag.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> ,TagRepositoryCustom{

	Optional<Tag> findByMemberIdAndName(Long memberId, String name);
}
