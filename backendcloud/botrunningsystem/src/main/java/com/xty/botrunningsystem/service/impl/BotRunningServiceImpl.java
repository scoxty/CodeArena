package com.xty.botrunningsystem.service.impl;

import com.xty.botrunningsystem.service.BotRunningService;
import com.xty.botrunningsystem.service.impl.utils.Bot;
import com.xty.botrunningsystem.service.impl.utils.BotPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotRunningServiceImpl implements BotRunningService {
    @Autowired
    private BotPool botPool;

    @Override
    public String addBot(Integer userId, String type, String botCode, String input) {
        Bot bot = new Bot(userId, type, botCode, input, null, null, null, null);
        botPool.addBot(bot);
        return "add bot success";
    }

    @Override
    public String addBotWithAI(Integer userId, String type, String botCode, String input, Integer aiId, String type2, String aiBotCode, String input2) {
        Bot bot = new Bot(userId, type, botCode, input, aiId, type2, aiBotCode, input2);
        botPool.addBot(bot);
        return "add bot success";
    }
}
