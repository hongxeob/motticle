package com.hongxeob.motticle.auth.application;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.hongxeob.motticle.auth.MyOAuth2member;
import com.hongxeob.motticle.auth.OAuth2Attribute;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final MemberRepository memberRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// 기본 OAuth2UserService 객체 생성
		OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();

		// Oauth2UserService를 사용하여 Oauth2User 정보를 가져온다.
		OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

		// 클라이언트 등록 ID(google, naver, kakao)와 사용자 이름 속성을 가져온다.
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

		OAuth2Attribute oAuth2Attribute = OAuth2Attribute.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

		Member member = saveOrUpdate(oAuth2Attribute);

		return new MyOAuth2member(
			Collections.singleton(new SimpleGrantedAuthority(member.getRole().getKey())), oAuth2Attribute.getAttributeKey(), member);
	}

	private Member saveOrUpdate(OAuth2Attribute attribute) {
		Member member = memberRepository.findByEmail(attribute.getEmail())
			.orElse(attribute.toEntity());

		return memberRepository.save(member);
	}
}
