package com.xty.backend.service.impl.user.bot;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xty.backend.mapper.BotMapper;
import com.xty.backend.pojo.Bot;
import com.xty.backend.pojo.User;
import com.xty.backend.service.impl.utils.UserDetailsImpl;
import com.xty.backend.service.user.bot.GetListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetListServiceImpl implements GetListService {

    @Autowired
    private BotMapper botMapper;

    @Override
    public List<Bot> getList() {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();

        LambdaQueryWrapper<Bot> botLambdaQueryWrapper = new LambdaQueryWrapper<>();
        botLambdaQueryWrapper.eq(Bot::getUserId, user.getId());
        List<Bot> bots = botMapper.selectList(botLambdaQueryWrapper);

        return bots;
    }
}
