package com.xty.matchingsystem.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class IpAddressFilter implements Filter {
    private final static Set<String> ipWhiteList = Set.of(
            "127.0.0.1",
            "0:0:0:0:0:0:0:1" // localhost
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (!ipWhiteList.contains(req.getRemoteAddr())) {
            System.out.println("拦截非法ip: " + req.getRemoteAddr());
            return;
        }

        chain.doFilter(request, response);
    }
}