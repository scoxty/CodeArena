package com.xty.botrunningsystem;

import com.xty.botrunningsystem.service.impl.BotRunningServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BotRunningSystemApplication {
    public static void main(String[] args) {
        BotRunningServiceImpl.botPool.start();
        BotRunningServiceImpl.botPoolWithAI.start();
        SpringApplication.run(BotRunningSystemApplication.class, args);
        System.out.println("BotRunningSystemApplication启动完成!");
    }
}
