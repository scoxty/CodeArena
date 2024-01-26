package com.xty.backend.service.impl.pk;

import com.xty.backend.websocket.WebSocketServer;
import com.xty.backend.websocket.utils.Game;
import com.xty.backend.service.pk.ReceiveBotMoveService;
import org.springframework.stereotype.Service;

@Service
public class ReceiveBotMoveServiceImpl implements ReceiveBotMoveService {

    @Override
    public String receiveBotMove(Integer userId, Integer direction) {
        System.out.println("receive bot move: " + userId + " " + direction);
        if (WebSocketServer.users.get(userId) != null) {
            Game game = WebSocketServer.users.get(userId).game;
            if (game != null) {
                if (game.getPlayerA().getId().equals(userId)) {
                    game.setNextStepA(direction);
                } else if (game.getPlayerB().getId().equals(userId)) {
                    game.setNextStepB(direction);
                }
            }
        }
        return "receive bot move success";
    }

    @Override
    public String receiveBotMoveWithAI(Integer userIdA, Integer directionA, Integer userIdB, Integer directionB) {
        System.out.println("Human-machine combat");
        System.out.println("receive human move: " + userIdA + " " + directionA);
        System.out.println("receive bot move: " + userIdB + " " + directionB);
        if (WebSocketServer.users.get(userIdA) != null) {
            Game game = WebSocketServer.users.get(userIdA).game;
            if (game != null) {
                if (directionA != -1) {
                    game.setNextStepA(directionA);
                }
                game.setNextStepB(directionB); // 由于人机是在用户匹配过长时间时才提供的，因此后匹配的B一定是AI。
            }
        }
        return "receive bot move success";
    }
}
