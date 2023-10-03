package com.xty.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MyTest {
    @Test
    public void test() {
        //创建解析器
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        //对密码进行加密
        String password = encoder.encode("123456");
        System.out.println("------------"+password);

//        //判断原字符加密后和内容是否匹配
//        boolean result = encoder.matches("123",password);
//        System.out.println("============="+result);
    }
}