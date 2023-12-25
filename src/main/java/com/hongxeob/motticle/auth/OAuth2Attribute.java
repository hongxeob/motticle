package com.hongxeob.motticle.auth;

import java.util.HashMap;
import java.util.Map;

import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.Role;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Builder
@Getter
public class OAuth2Attribute {
	private Map<String, Object> attributes; // 사용자 속성 정보를 담는 Map
	private String attributeKey; // 사용자 속성의 키 값
	private String name; // 이름 정보
	private String nickname;
	private String email; // 이메일 정보
	private String genderType;
	private String picture; // 프로필 사진 정보
	private String provider; // 제공자 정보

	public static OAuth2Attribute of(String provider, String attributeKey, Map<String, Object> attributes) {
		switch (provider) {
			case "kakao":
				return ofKakao(provider, "email", attributes);
			default:
				throw new RuntimeException();
		}
	}

	/*
	 *   Kakao 로그인일 경우 사용하는 메서드, 필요한 사용자 정보가 kakaoAccount -> kakaoProfile 두번 감싸져 있어서,
	 *   두번 get() 메서드를 이용해 사용자 정보를 담고있는 Map을 꺼내야한다.
	 * */
	private static OAuth2Attribute ofKakao(String provider, String attributeKey, Map<String, Object> attributes) {
		Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
		Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
		Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

		return OAuth2Attribute.builder()
			.nickname((String) properties.get("nickname"))
			.email((String) kakaoAccount.get("email"))
			.provider(provider)
			.attributes(kakaoAccount)
			.attributeKey(attributeKey)
			.build();
	}

	// OAuth2User 객체에 넣어주기 위해서 Map으로 값들을 반환해준다.
	public Map<String, Object> convertToMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("id", attributeKey);
		map.put("key", attributeKey);
		map.put("email", email);
		map.put("provider", provider);

		return map;
	}

	/*
	 * 기존에 자사 서비스를 가입 한 적이 없는 사람들을 위한 회원 가입 1차 과정
	 * 본격적인 활동은 Member 권한이 있어야 한다.
	 * 회원에 필요한 값들을 채우기 전에는 GUEST 권한을 갖는다.
	 */
	public Member toEntity() {
		return Member.builder()
			.nickname(nickname)
			.email(email)
			.role(Role.GUEST)
			.build();
	}
}
