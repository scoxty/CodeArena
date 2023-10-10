package com.xty.backend.service.impl.user.bot;

import com.xty.backend.mapper.BotMapper;
import com.xty.backend.pojo.Bot;
import com.xty.backend.pojo.User;
import com.xty.backend.service.impl.utils.UserDetailsImpl;
import com.xty.backend.service.user.bot.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UpdateServiceImpl implements UpdateService {

    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> update(Map<String, String> req) {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();

        int bot_id = Integer.parseInt(req.get("bot_id"));
        String title = req.get("title");
        String description = req.get("description");
        String content = req.get("content");
        Date now = new Date();

        Map<String, String> resp = new HashMap<>();

        if (title == null) {
            resp.put("error_msg", "标题参数为null!");
            return resp;
        }
        if (title.length() == 0) {
            resp.put("error_msg", "标题不能为空!");
            return resp;
        }
        if (title.length() > 100) {
            resp.put("error_msg", "标题长度不能大于100");
            return resp;
        }

        if (description == null) {
            resp.put("error_msg", "Bot的描述参数为null");
            return resp;
        }
        if (description.length() == 0) {
            resp.put("error_msg", "Bot的描述不能为空");
        }
        if (description.length() > 300) {
            resp.put("error_msg", "Bot的描述长度不能大于300");
            return resp;
        }

        if (content == null) {
            resp.put("error_msg", "Bot的content参数为null");
            return resp;
        }
        if (content.length() == 0) {
            resp.put("error_msg", "Bot的content不能为空");
            return resp;
        }
        if (content.length() > 10000) {
            resp.put("error_msg", "Bot的content长度不能大于10000");
            return resp;
        }

        Bot bot = botMapper.selectById(bot_id);
        if (bot == null) {
            resp.put("error_msg", "Bot不存在或已被删除!");
            return resp;
        }
        if (!bot.getUserId().equals(user.getId())) {
            resp.put("error_msg", "非Bot作者，没有权限修改!");
            return resp;
        }

        Bot new_bot = new Bot(
                bot.getId(),
                user.getId(),
                title,
                description,
                content,
                bot.getCreatetime(),
                now
        );

        botMapper.updateById(new_bot);
        resp.put("error_msg", "success");

        return resp;
    }
}
