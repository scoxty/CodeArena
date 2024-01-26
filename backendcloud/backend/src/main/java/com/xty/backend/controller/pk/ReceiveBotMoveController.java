package com.xty.backend.controller.pk;

import com.xty.backend.service.pk.ReceiveBotMoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class ReceiveBotMoveController {
    @Autowired
    private ReceiveBotMoveService receiveBotMoveService;

    @PostMapping("/pk/receive/bot/move")
    public String receiveBotMove(@RequestParam MultiValueMap<String, String> req) {
        Integer userId = Integer.parseInt(Objects.requireNonNull(req.getFirst("user_id")));
        Integer direction = Integer.parseInt(Objects.requireNonNull(req.getFirst("direction")));
        return receiveBotMoveService.receiveBotMove(userId, direction);
    }

    @PostMapping("/pkWithAI/receive/bot/move")
    public String receiveBotMoveWithAI(@RequestParam MultiValueMap<String, String> req) {
        Integer userIdA = Integer.parseInt(Objects.requireNonNull(req.getFirst("user_id_a")));
        Integer directionA = Integer.parseInt(Objects.requireNonNull(req.getFirst("direction_a")));
        Integer userIdB = Integer.parseInt(Objects.requireNonNull(req.getFirst("user_id_b")));
        Integer directionB = Integer.parseInt(Objects.requireNonNull(req.getFirst("direction_b")));
        return receiveBotMoveService.receiveBotMoveWithAI(userIdA, directionA, userIdB, directionB);
    }
}
