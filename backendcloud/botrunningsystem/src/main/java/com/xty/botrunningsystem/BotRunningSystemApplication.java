package com.xty.botrunningsystem;

import com.xty.botrunningsystem.service.impl.BotRunningServiceImpl;
import com.xty.botrunningsystem.service.impl.utils.BotPool;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BotRunningSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotRunningSystemApplication.class, args);
        System.out.println("BotRunningSystemApplication启动完成!");
    }
}
