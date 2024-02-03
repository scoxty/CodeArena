package com.xty.botrunningsystem.service.impl.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class BotPool {
    private final BlockingQueue<Bot> bots = new LinkedBlockingQueue<>();

    @Autowired
    @Qualifier("botTaskExecutor")
    private Executor botTaskExecutor;

    @Autowired
    private Consumer consumer;

    @PostConstruct
    public void init() {
        botTaskExecutor.execute(this::processBots);
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
                // 比较耗时，放后面
                if (bot.getAiId() == null) {
                    consumer.startTimeOut(2000, bot);
                } else {
                    consumer.startTimeOut(4000, bot); // 执行用户和人机的共两份代码
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
