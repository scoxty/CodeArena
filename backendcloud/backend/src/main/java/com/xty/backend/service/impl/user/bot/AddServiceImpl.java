package com.xty.backend.service.impl.user.bot;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xty.backend.mapper.BotMapper;
import com.xty.backend.pojo.Bot;
import com.xty.backend.pojo.User;
import com.xty.backend.service.impl.utils.UserDetailsImpl;
import com.xty.backend.service.user.bot.AddService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AddServiceImpl implements AddService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> add(Map<String, String> req) {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();

        String title = req.get("title");
        String description = req.get("description");
        String content = req.get("content");
        String type = req.get("type");

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

        if (type == null) {
            resp.put("error_msg", "Bot的type参数为null");
            return resp;
        }
        if (type.length() == 0) {
            resp.put("error_msg", "请选择编程语言");
            return resp;
        }
        if (type.length() > 15) {
            resp.put("error_msg", "type的长度不能大于15");
            return resp;
        }


        LambdaQueryWrapper<Bot> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Bot::getUserId, user.getId());
        if (botMapper.selectCount(lambdaQueryWrapper) >= 5) {
            resp.put("error_msg", "每个用户至多创建5个bot");
            return resp;
        }

        Date now = new Date();
        Bot bot = new Bot(null, user.getId(), title, description, content, type, now, now);

        botMapper.insert(bot);
        resp.put("error_msg", "success");

        return resp;
    }
}
