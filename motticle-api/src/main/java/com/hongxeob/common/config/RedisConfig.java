package com.hongxeob.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongxeob.notification.res.NotificationRes;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableRedisRepositories
@RequiredArgsConstructor
public class RedisConfig {

	@Value("${spring.redis.host}")
	private String host;

	@Value("${spring.redis.port}")
	private int port;

	@Value("${spring.redis.password}")
	private String password;

	@Bean
	public RedisConnectionFactory tokenRedisConnectionFactory() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(host);
		redisStandaloneConfiguration.setPort(port);
		redisStandaloneConfiguration.setPassword(password);
		return new LettuceConnectionFactory(redisStandaloneConfiguration);
	}

	@Bean
	public RedisTemplate<?, ?> redisTemplate(@Qualifier("tokenRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));

		return redisTemplate;
	}

	@Bean
	public RedisConnectionFactory notificationRedisConnectionFactory() {
		return new LettuceConnectionFactory(host, port);
	}

	@Bean
	public RedisOperations<String, NotificationRes> eventRedisOperations(
		@Qualifier("notificationRedisConnectionFactory") RedisConnectionFactory notificationRedisConnectionFactory,
		ObjectMapper objectMapper) {
		Jackson2JsonRedisSerializer<NotificationRes> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
			NotificationRes.class);
		jsonRedisSerializer.setObjectMapper(objectMapper);
		RedisTemplate<String, NotificationRes> eventRedisTemplate = new RedisTemplate<>();
		eventRedisTemplate.setConnectionFactory(notificationRedisConnectionFactory);
		eventRedisTemplate.setKeySerializer(RedisSerializer.string());
		eventRedisTemplate.setValueSerializer(jsonRedisSerializer);
		eventRedisTemplate.setHashKeySerializer(RedisSerializer.string());
		eventRedisTemplate.setHashValueSerializer(jsonRedisSerializer);

		return eventRedisTemplate;
	}

	@Bean
	public RedisMessageListenerContainer notificationRedisMessageListenerContainer(
		@Qualifier("notificationRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
		redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);

		return redisMessageListenerContainer;
	}
}
