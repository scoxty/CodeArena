package com.xty.backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class IpInterceptor implements HandlerInterceptor {
    private final static Set<String> ipWhiteList = Set.of(
            "127.0.0.1",
            "0:0:0:0:0:0:0:1" // localhost
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!ipWhiteList.contains(request.getRemoteAddr())) {
            System.out.println("拦截非法ip: " + request.getRemoteAddr());
            return false;
        }
        return true;
    }
}
