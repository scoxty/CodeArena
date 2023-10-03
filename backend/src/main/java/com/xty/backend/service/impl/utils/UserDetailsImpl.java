package com.xty.backend.service.impl.utils;

import com.xty.backend.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // 获取权限
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { // 账号是否过期
        return true;
    }

    @Override
    public boolean isAccountNonLocked() { // 账户是否被锁定
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() { // 授权是否过期
        return true;
    }

    @Override
    public boolean isEnabled() { // 用户是否被启用
        return true;
    }
}
