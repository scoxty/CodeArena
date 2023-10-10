package com.xty.matchingsystem.config;

import com.xty.matchingsystem.filter.IpAddressFilter;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private IpAddressFilter ipAddressFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(CsrfConfigurer::disable) // 基于token，不需要csrf
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))// 基于token，不需要session
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/player/add", "/player/remove").permitAll() // 放行api
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}