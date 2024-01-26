package com.xty.botrunningsystem.service.impl;

import com.xty.botrunningsystem.service.BotRunningService;
import com.xty.botrunningsystem.service.impl.utils.BotPool;
import com.xty.botrunningsystem.service.impl.utils.BotPoolWithAI;
import org.springframework.stereotype.Service;

@Service
public class BotRunningServiceImpl implements BotRunningService {
    public final static BotPool botPool = new BotPool();
    public final static BotPoolWithAI botPoolWithAI = new BotPoolWithAI();

    @Override
    public String addBot(Integer userId, String botCode, String input) {
        botPool.addBot(userId, botCode, input);
        return "add bot success";
    }

    @Override
    public String addBotWithAI(Integer userId, String botCode, String input, Integer aiId, String aiBotCode, String input2) {
        botPoolWithAI.addBotWithAI(userId, botCode, input, aiId, aiBotCode, input2);
        return "add bot success";
    }
}
