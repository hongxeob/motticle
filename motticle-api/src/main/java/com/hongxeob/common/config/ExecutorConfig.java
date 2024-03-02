package com.hongxeob.common.config;

import java.time.Duration;

import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorConfig {

	@Bean
	public ThreadPoolTaskExecutor threadPoolExecutor() {
		return new TaskExecutorBuilder()
			.corePoolSize(10)
			.maxPoolSize(10)
			.queueCapacity(200)
			.awaitTermination(true)
			.awaitTerminationPeriod(Duration.ofSeconds(10))
			.threadNamePrefix("task-executor-")
			.build();
	}
}
