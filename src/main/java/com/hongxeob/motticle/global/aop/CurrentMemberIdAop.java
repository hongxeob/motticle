package com.hongxeob.motticle.global.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.hongxeob.motticle.auth.application.dto.SecurityMemberDto;
import com.hongxeob.motticle.auth.token.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CurrentMemberIdAop {

	private static final String MEMBER_ID = "memberId";
	private final JwtUtil jwtUtil;

	@Around("@annotation(currentMemberId)")
	public Object getCurrentUserId(ProceedingJoinPoint proceedingJoinPoint,
								   CurrentMemberId currentMemberId) throws Throwable {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();

		String token = jwtUtil.resolveAccessToken(request);
		Authentication authentication = jwtUtil.getAccessTokenAuthentication(token);
		Long memberId = ((SecurityMemberDto) authentication.getPrincipal()).memberId();

		Object[] modifiedArgs = modifyArgsWithMemberId(memberId, proceedingJoinPoint);
		return proceedingJoinPoint.proceed(modifiedArgs);
	}

	private Object[] modifyArgsWithMemberId(long memberId, ProceedingJoinPoint proceedingJoinPoint) {
		Object[] parameters = proceedingJoinPoint.getArgs();

		MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
		Method method = signature.getMethod();
		Parameter[] methodParameters = method.getParameters();

		for (int i = 0; i < methodParameters.length; i++) {
			String parameterName = methodParameters[i].getName();
			if (parameterName.equals(MEMBER_ID)) {
				parameters[i] = memberId;
			}
		}
		return parameters;
	}
}
