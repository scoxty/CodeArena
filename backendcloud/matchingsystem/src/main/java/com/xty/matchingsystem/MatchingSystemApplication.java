package com.xty.matchingsystem;

import com.xty.matchingsystem.service.impl.MatchingServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MatchingSystemApplication {
    public static void main(String[] args) {
        MatchingServiceImpl.matchingpool.start(); // 启动匹配线程
        SpringApplication.run(MatchingSystemApplication.class, args);
    }
}
