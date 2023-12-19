package com.hongxeob.motticle.tag.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

	// TODO: 12/17/23 무한 스크롤로 변경
	List<Tag> findAllByMemberId(Long memberId);

	Optional<Tag> findByMemberIdAndName(Long memberId, String name);
}
