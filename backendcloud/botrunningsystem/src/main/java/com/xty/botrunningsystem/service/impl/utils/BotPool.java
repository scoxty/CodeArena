package com.xty.botrunningsystem.service.impl.utils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@Component
public class BotPool {
    private final BlockingQueue<Bot> bots = new LinkedBlockingQueue<>(100);

    @Autowired
    @Qualifier("botQueueExecutor")
    private ThreadPoolExecutor botQueueExecutor;

    @Autowired
    private Consumer consumer;

    @PostConstruct
    public void init() {
        // 使用多个线程处理Bot队列
        IntStream.range(0, Runtime.getRuntime().availableProcessors()).forEach(i ->
                botQueueExecutor.submit(this::processBots));
    }

    public void addBot(Bot bot) {
        try {
            bots.put(bot);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processBots() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Bot bot = bots.take();
                consumer.consumeBot(bot);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @PreDestroy
    public void destroy() {
        // 清理线程池资源
        botQueueExecutor.shutdown();
        try {
            if (!botQueueExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                botQueueExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            botQueueExecutor.shutdownNow();
        }
    }
}
