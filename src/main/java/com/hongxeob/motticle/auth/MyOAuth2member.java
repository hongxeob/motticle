package com.hongxeob.motticle.auth;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import com.hongxeob.motticle.member.domain.GenderType;
import com.hongxeob.motticle.member.domain.Member;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class MyOAuth2member implements OAuth2User, Serializable {

	@Serial
	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	private final Set<GrantedAuthority> authorities;
	private final String nameAttributeKey;
	private final Long memberId;
	private final String email;
	private final GenderType genderType;
	private String nickName;

	public MyOAuth2member(Collection<? extends GrantedAuthority> authorities,
						  String nameAttributeKey, Member member) {
		Assert.notNull(member, "member cannot be empty");
		Assert.hasText(nameAttributeKey, "nameAttributeKey cannot be empty");
		this.authorities = (authorities != null)
			? Collections.unmodifiableSet(new LinkedHashSet<>(this.sortAuthorities(authorities)))
			: Collections.unmodifiableSet(new LinkedHashSet<>(AuthorityUtils.NO_AUTHORITIES));
		this.nameAttributeKey = nameAttributeKey;
		this.memberId = member.getId();
		this.nickName = member.getNickname();
		this.email =member.getEmail();
		this.genderType = member.getGenderType();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return Collections.emptyMap();
	}

	@Override
	public String getName() {
		return this.nameAttributeKey;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	private Set<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
		SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(
			Comparator.comparing(GrantedAuthority::getAuthority));
		sortedAuthorities.addAll(authorities);
		return sortedAuthorities;
	}
}
