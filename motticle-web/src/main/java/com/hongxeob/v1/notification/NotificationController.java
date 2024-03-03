package com.hongxeob.v1.notification;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.hongxeob.common.aop.CurrentMemberId;
import com.hongxeob.notification.NotificationService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping(value = "/subscribe", produces = "text/event-stream")
	@CurrentMemberId
	public SseEmitter subscribe(Long memberId) throws IOException {
		return notificationService.subscribe(memberId);
	}
}
