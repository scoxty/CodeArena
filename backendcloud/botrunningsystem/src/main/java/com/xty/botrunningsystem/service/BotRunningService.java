package com.xty.botrunningsystem.service;

public interface BotRunningService {
    String addBot(Integer userId, String type, String botCode, String input);
    String addBotWithAI(Integer userId, String type, String botCode, String input, Integer aiId, String type2, String aiBotCode, String input2);
}
