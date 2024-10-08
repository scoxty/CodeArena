package com.xty.botrunningsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class SandBoxThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor sandboxExecutor() {
        return new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() * 2,
                Runtime.getRuntime().availableProcessors() * 4,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
