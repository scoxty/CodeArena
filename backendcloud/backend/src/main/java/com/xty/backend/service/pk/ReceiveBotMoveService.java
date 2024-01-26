package com.xty.backend.service.pk;

public interface ReceiveBotMoveService {
    String receiveBotMove(Integer userId, Integer direction);
    String receiveBotMoveWithAI(Integer userIdA, Integer directionA, Integer userIdB, Integer directionB);
}
