package com.xty.botrunningsystem.service;

public interface BotRunningService {
    String addBot(Integer userId, String botCode, String input);
    String addBotWithAI(Integer userId, String botCode, String input, Integer aiId, String aiBotCode, String input2);
}
