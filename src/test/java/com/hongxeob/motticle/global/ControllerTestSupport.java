package com.hongxeob.motticle.global;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongxeob.motticle.article.application.ArticleService;
import com.hongxeob.motticle.article.presentation.ArticleController;
import com.hongxeob.motticle.auth.MyOAuth2member;
import com.hongxeob.motticle.auth.OAuth2Attribute;
import com.hongxeob.motticle.auth.application.CustomOAuth2UserService;
import com.hongxeob.motticle.auth.application.RefreshTokenService;
import com.hongxeob.motticle.auth.handler.AuthenticationCustomSuccessHandler;
import com.hongxeob.motticle.auth.presentation.AuthController;
import com.hongxeob.motticle.auth.token.JwtUtil;
import com.hongxeob.motticle.auth.token.filter.JwtAuthFilter;
import com.hongxeob.motticle.member.application.MemberService;
import com.hongxeob.motticle.member.domain.GenderType;
import com.hongxeob.motticle.member.domain.Member;
import com.hongxeob.motticle.member.domain.MemberRepository;
import com.hongxeob.motticle.member.domain.Role;
import com.hongxeob.motticle.member.presentation.MemberController;
import com.hongxeob.motticle.scrap.application.ScrapService;
import com.hongxeob.motticle.scrap.presentation.ScrapController;
import com.hongxeob.motticle.tag.application.TagService;
import com.hongxeob.motticle.tag.presentation.TagController;

@WebMvcTest(
	value = {
		TagController.class, ArticleController.class, MemberController.class,
		ScrapController.class, AuthController.class
	}
)
@Import(ControllerTestSupport.SecurityConfig.class)
@ExtendWith({RestDocumentationExtension.class})
public abstract class ControllerTestSupport {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected JwtAuthFilter jwtAuthFilter;

	@MockBean
	protected TagService tagService;

	@MockBean
	protected JwtUtil jwtUtil;

	@MockBean
	protected MemberService memberService;

	@MockBean
	protected ArticleService articleService;

	@MockBean
	protected ScrapService scrapService;

	@MockBean
	protected RefreshTokenService refreshTokenService;

	@MockBean
	protected MemberRepository memberRepository;

	@MockBean
	protected SecurityContext securityContext;

	@MockBean
	protected CustomOAuth2UserService customOAuth2UserService;

	@MockBean
	protected AuthenticationCustomSuccessHandler successHandler;

	@Mock
	protected Authentication authentication;

	@TestConfiguration
	static class SecurityConfig {

		@Bean
		public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

			http
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

			http
				.authorizeHttpRequests(
					authorize -> authorize.anyRequest().permitAll()
				);

			return http.build();
		}
	}

	@BeforeEach
	void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

		Member member = mock(Member.class);
		given(member.getId()).willReturn(1L);
		given(member.getEmail()).willReturn("sjun2918@naver.com");
		given(member.getGenderType()).willReturn(GenderType.FEMALE);
		given(member.getNickname()).willReturn("호빵");
		given(member.getRole()).willReturn(Role.USER);
		given(authentication.getName()).willReturn(Role.USER.name());
		OAuth2Attribute oAuthAttributes = OAuth2Attribute.builder()
			.attributes(Collections.emptyMap())
			.email("email")
			.attributeKey("test")
			.name("name")
			.nickname("nickname")
			.genderType("genderType")
			.picture("picture")
			.provider("kakao")
			.build();

		MyOAuth2member myOAuth2Member = new MyOAuth2member(Collections.singleton(new SimpleGrantedAuthority(member.getRole().getKey())), oAuthAttributes.getAttributeKey(), member);

		when(authentication.getPrincipal()).thenReturn(myOAuth2Member);
		when(authentication.isAuthenticated()).thenReturn(true);
		when(authentication.getName()).thenReturn(Role.USER.name());
		securityContext.setAuthentication(authentication);

		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.addFilter(new CharacterEncodingFilter("UTF-8", true))
			.apply(springSecurity())
			.apply(documentationConfiguration(restDocumentation))
			.alwaysDo(print())
			.build();
	}
}
