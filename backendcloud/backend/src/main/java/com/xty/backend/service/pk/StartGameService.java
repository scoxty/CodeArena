package com.xty.backend.service.pk;

public interface StartGameService {
    String startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId);
    String startGameWithAI(Integer userId, Integer botId);
}
