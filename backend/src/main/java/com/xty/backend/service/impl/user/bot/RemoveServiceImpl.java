package com.xty.backend.service.impl.user.bot;

import com.xty.backend.mapper.BotMapper;
import com.xty.backend.pojo.Bot;
import com.xty.backend.pojo.User;
import com.xty.backend.service.impl.utils.UserDetailsImpl;
import com.xty.backend.service.user.bot.RemoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RemoveServiceImpl implements RemoveService {

    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> remove(Map<String, String> req) {
        int bot_id = Integer.parseInt(req.get("bit_id"));
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();

        Bot bot = botMapper.selectById(bot_id);

        Map<String, String> resp = new HashMap<>();

        if (bot == null) {
            resp.put("error_msg", "Bot不存在或已被删除!");
            return resp;
        }
        if (bot.getUserId() != user.getId()) {
            resp.put("error_msg", "你不是Bot的作者，没有权限删除!");
            return resp;
        }

        botMapper.deleteById(bot_id);
        resp.put("error_msg", "success");

        return resp;
    }
}
