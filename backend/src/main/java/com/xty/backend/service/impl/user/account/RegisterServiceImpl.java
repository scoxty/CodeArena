package com.xty.backend.service.impl.user.account;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xty.backend.mapper.UserMapper;
import com.xty.backend.pojo.User;
import com.xty.backend.service.user.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, String> register(String username, String password, String confirmedPassword) {
        Map<String, String> resp = new HashMap<>();

        if (username == null) {
            resp.put("error_msg", "用户名为空指针!");
            return resp;
        }
        username = username.trim(); // 删除头尾空白字符
        if (username.length() == 0) {
            resp.put("error_msg", "用户名不能仅包含空白字符!");
            return resp;
        }
        if (username.length() > 100) {
            resp.put("error_msg", "用户名长度不能大于100!");
            return resp;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            resp.put("error_msg", "用户名已存在!");
            return resp;
        }

        if (password == null || confirmedPassword == null) {
            resp.put("error_msg", "密码为空指针!");
            return resp;
        }
        if (password == "" || confirmedPassword == "") {
            resp.put("error_msg", "密码不能为空!");
            return resp;
        }
        if (password.length() > 100 || confirmedPassword.length() > 100) {
            resp.put("error_msg", "密码的长度不能大于100!");
            return resp;
        }
        if (!password.equals(confirmedPassword)) {
            resp.put("error_msg", "两次输入的密码不一致!");
            return resp;
        }

        String encodePassword = passwordEncoder.encode(password);
        String photo = "https://img-qn.51miz.com/Element/00/18/44/19/187dcc75_E184419_a20ab3dd.png";
        User user1 = new User(null, username, encodePassword, photo);

        userMapper.insert(user1);

        resp.put("error_msg", "success");

        return resp;
    }
}
