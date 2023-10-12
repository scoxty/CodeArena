package com.xty.backend.service.impl.ranklist;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xty.backend.mapper.UserMapper;
import com.xty.backend.pojo.User;
import com.xty.backend.service.ranklist.GetRankListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetRankListServiceImpl implements GetRankListService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public JSONObject getList(Integer page) {
        IPage<User> userIPage = new Page<>(page, 3);
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(User::getRating);
        List<User> users = userMapper.selectPage(userIPage, lambdaQueryWrapper).getRecords();
        for (User user: users) {
            user.setPassword("");
        }
        JSONObject resp = new JSONObject();
        resp.put("users", users);
        resp.put("users_count", userIPage.getTotal());
        return resp;
    }
}
