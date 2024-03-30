package com.hongxeob.notification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongxeob.article.ArticleService;
import com.hongxeob.domain.article.Article;
import com.hongxeob.domain.enumeration.ErrorCode;
import com.hongxeob.domain.exception.BusinessException;
import com.hongxeob.domain.member.Member;
import com.hongxeob.domain.notification.Notification;
import com.hongxeob.domain.notification.NotificationRepository;
import com.hongxeob.member.MemberService;
import com.hongxeob.notification.req.NotificationEvent;
import com.hongxeob.notification.res.NotificationRes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class NotificationService {

	private static final String REDIS_CHANNEL_PREFIX = "notification:";
	private static final long DEFAULT_TIMEOUT = 10L * 1000 * 60;
	private static final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

	private final MemberService memberService;
	private final ArticleService articleService;
	private final NotificationRepository notificationRepository;
	private final RedisMessageListenerContainer redisMessageListenerContainer;
	private final RedisOperations<String, NotificationRes> eventRedisOperations;
	private final ObjectMapper objectMapper;

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void send(NotificationEvent notificationEvent) {
		Notification savedNotification = notificationRepository.save(createNotification(notificationEvent));
		NotificationRes notificationRes = NotificationRes.from(savedNotification);

		final String channelId = String.valueOf(notificationEvent.memberId());
		String channelName = getChannelName(channelId);

		log.info("Generated JSON for Redis: {}", notificationRes);

		eventRedisOperations.convertAndSend(channelName, notificationRes);
		log.info("Notification sent to Redis channel {}: {}", channelId, notificationRes);
	}

	public SseEmitter subscribe(Long memberId) {
		String id = String.valueOf(memberId);
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

		try {
			emitter.send(SseEmitter.event()
				.id(id)
				.name("sse"));
			emitters.add(emitter);
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.SSE_STREAM_ERROR);
		}

		MessageListener messageListener = (message, pattern) -> {
			final NotificationRes notificationRes = serialize(message);
			sendToClient(emitter, id, notificationRes);
		};

		this.redisMessageListenerContainer.addMessageListener(messageListener, ChannelTopic.of(getChannelName(id)));
		checkEmitterStatus(emitter, messageListener);

		return emitter;
	}

	private Notification createNotification(NotificationEvent notificationEvent) {
		Article article = articleService.getArticle(notificationEvent.ArticleId());
		Member member = memberService.getMember(notificationEvent.memberId());
		String message = Notification.createMessage(notificationEvent.notificationType(), article);

		return Notification.builder()
			.member(member)
			.article(article)
			.notificationType(notificationEvent.notificationType())
			.content(message)
			.build();
	}

	private NotificationRes serialize(Message message) {
		try {
			String json = new String(message.getBody(), StandardCharsets.UTF_8);
			log.info("message body => {},message channel => {}", message.getBody(), message.getChannel());
			log.info("JSON received: {}", json);
			NotificationRes notificationRes = objectMapper.readValue(json, NotificationRes.class);
			log.info("serial => {}", notificationRes.toString());
			return notificationRes;
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.INVALID_REDIS_MESSAGE_FORMAT);
		}
	}

	private void sendToClient(SseEmitter emitter, String id, Object data) {
		try {
			emitter.send(SseEmitter.event()
				.id(id)
				.name("sse")
				.data(data));
		} catch (IOException e) {
			emitters.remove(emitter);
			log.error("SSE 연결이 올바르지 않습니다. 해당 memberID => {}", id);
		}
	}

	private void checkEmitterStatus(SseEmitter emitter, MessageListener messageListener) {
		emitter.onCompletion(() -> {
			emitters.remove(emitter);
			this.redisMessageListenerContainer.removeMessageListener(messageListener);
		});
		emitter.onTimeout(() -> {
			emitters.remove(emitter);
			this.redisMessageListenerContainer.removeMessageListener(messageListener);
		});
	}

	private String getChannelName(final String memberId) {
		return REDIS_CHANNEL_PREFIX + memberId;
	}

}
