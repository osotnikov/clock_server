package com.osotnikov.clockserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@Configuration
public class ClockServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClockServerApplication.class, args);
	}

	/**
	 * Should be in a separate class but I wanted to check whether putting it in the main class would create problems.
	 * */
	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
		ThreadPoolTaskScheduler threadPoolTaskScheduler
			= new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(5);
		threadPoolTaskScheduler.setThreadNamePrefix(
			"ThreadPoolTaskScheduler");
		return threadPoolTaskScheduler;
	}

}
