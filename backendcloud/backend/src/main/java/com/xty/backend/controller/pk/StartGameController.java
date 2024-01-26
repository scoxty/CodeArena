package com.xty.backend.controller.pk;

import com.xty.backend.service.pk.StartGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class StartGameController {
    @Autowired
    private StartGameService startGameService;

    @PostMapping("/pk/start/game")
    public String startGame(@RequestParam MultiValueMap<String, String> req) {
        Integer aId = Integer.parseInt(Objects.requireNonNull(req.getFirst("a_id")));
        Integer aBotId = Integer.parseInt(Objects.requireNonNull(req.getFirst("a_bot_id")));
        Integer bId = Integer.parseInt(Objects.requireNonNull(req.getFirst("b_id")));
        Integer bBotId = Integer.parseInt(Objects.requireNonNull(req.getFirst("b_bot_id")));
        return startGameService.startGame(aId, aBotId, bId, bBotId);
    }

    @PostMapping("/pkWithAI/start/game")
    public String startGameWithAI(@RequestParam MultiValueMap<String, String> req) {
        Integer userId = Integer.parseInt(Objects.requireNonNull(req.getFirst("user_id")));
        Integer botId = Integer.parseInt(Objects.requireNonNull(req.getFirst("bot_id")));
        return startGameService.startGameWithAI(userId, botId);
    }
}
