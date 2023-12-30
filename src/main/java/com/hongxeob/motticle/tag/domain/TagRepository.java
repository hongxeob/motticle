package com.hongxeob.motticle.tag.domain;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

	Slice<Tag> findAllByMemberId(Long memberId, Pageable pageable);

	Optional<Tag> findByMemberIdAndName(Long memberId, String name);
}
