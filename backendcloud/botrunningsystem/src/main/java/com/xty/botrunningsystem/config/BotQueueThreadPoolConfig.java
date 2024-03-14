package com.xty.botrunningsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class BotQueueThreadPoolConfig {
    @Bean(name = "botQueueExecutor")
    public ThreadPoolExecutor botQueueExecutor() {
        return new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
