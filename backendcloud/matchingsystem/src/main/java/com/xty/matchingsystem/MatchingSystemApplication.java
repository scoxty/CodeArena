package com.xty.matchingsystem;

import com.xty.matchingsystem.service.impl.MatchingServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MatchingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(MatchingSystemApplication.class, args);
        System.out.println("MatchingSystemApplication启动完成!");
    }
}
