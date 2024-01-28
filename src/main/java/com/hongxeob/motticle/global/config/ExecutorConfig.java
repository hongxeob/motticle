package com.hongxeob.motticle.global.config;

import java.time.Duration;

import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorConfig {

	//todo 스프링 비동기 @Async 사용 고려 / 적절한 스레드풀 사이즈 고려
	@Bean
	public ThreadPoolTaskExecutor threadPoolExecutor() {
		return new TaskExecutorBuilder()
			.corePoolSize(12)
			.maxPoolSize(12)
			.queueCapacity(200)
			.awaitTermination(true)
			.awaitTerminationPeriod(Duration.ofSeconds(10))
			.threadNamePrefix("task-executor-")
			.build();
	}
}
