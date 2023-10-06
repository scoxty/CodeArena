package com.xty.backend.service.impl.user.bot;

import com.xty.backend.mapper.BotMapper;
import com.xty.backend.pojo.Bot;
import com.xty.backend.pojo.User;
import com.xty.backend.service.impl.utils.UserDetailsImpl;
import com.xty.backend.service.user.bot.AddService;
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

        Map<String, String> resp = new HashMap<>();

        if (title != null && title.length() > 100) {
            resp.put("error_msg", "标题长度不能大于100");
            return resp;
        }

        if (description != null && description.length() > 300) {
            resp.put("error_msg", "Bot的描述长度不能大于300");
            return resp;
        }

        if (content != null && content.length() > 10000) {
            resp.put("error_msg", "Bot的content长度不能大于10000");
            return resp;
        }

        Date now = new Date();
        Bot bot = new Bot(null, user.getId(), title, description, content, 1500, now, now);

        botMapper.insert(bot);
        resp.put("error_msg", "success");

        return resp;
    }
}
