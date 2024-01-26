package com.xty.backend.service.impl.user.account;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xty.backend.mapper.UserMapper;
import com.xty.backend.pojo.User;
import com.xty.backend.service.impl.utils.UserDetailsImpl;
import com.xty.backend.service.user.account.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InfoServiceImpl implements InfoService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, String> getInfo() {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();
        Map<String, String> resp = new HashMap<>();
        resp.put("error_msg", "success");
        resp.put("id", user.getId().toString());
        resp.put("username", user.getUsername());
        resp.put("photo", user.getPhoto());
        return resp;
    }
}
