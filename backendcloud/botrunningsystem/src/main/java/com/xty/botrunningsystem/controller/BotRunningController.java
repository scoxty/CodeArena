package com.xty.botrunningsystem.controller;

import com.xty.botrunningsystem.service.BotRunningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class BotRunningController {
    @Autowired
    private BotRunningService botRunningService;

    @PostMapping("/bot/add")
    public String addBot(@RequestParam MultiValueMap<String, String> req) {
        Integer userId = Integer.parseInt(Objects.requireNonNull(req.getFirst("user_id")));
        String botCode = req.getFirst("bot_code");
        String input = req.getFirst("input");
        return botRunningService.addBot(userId, botCode, input);
    }

    @PostMapping("/botWithAI/add")
    public String addBotWithAI(@RequestParam MultiValueMap<String, String> req) {
        Integer userId = Integer.parseInt(Objects.requireNonNull(req.getFirst("user_id")));
        String botCode = req.getFirst("bot_code");
        String input = req.getFirst("input");
        Integer aiId = Integer.parseInt(Objects.requireNonNull(req.getFirst("ai_id")));
        String aiBotCode = req.getFirst("ai_bot_code");
        String input2 = req.getFirst("input2");
        return botRunningService.addBotWithAI(userId, botCode, input, aiId, aiBotCode, input2);
    }
}
