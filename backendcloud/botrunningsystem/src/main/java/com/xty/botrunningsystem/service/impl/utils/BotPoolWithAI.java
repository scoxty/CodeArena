package com.xty.botrunningsystem.service.impl.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPoolWithAI extends Thread{
    private final ReentrantLock lockWithAI = new ReentrantLock();
    private final Condition conditionWithAI = lockWithAI.newCondition();
    private final Queue<BotWithAI> botsWithAI = new LinkedList<>();

    public void addBotWithAI(Integer userId, String botCode, String input, Integer aiId, String aiBotCode, String input2) {
        lockWithAI.lock();
        try {
            botsWithAI.add(new BotWithAI(userId, botCode, input, aiId, aiBotCode, input2));
            conditionWithAI.signalAll();
        } finally {
            lockWithAI.unlock();
        }
    }

    private void consumeWithAI(BotWithAI botWithAI) {
        ConsumerWithAI consumerWithAI = new ConsumerWithAI();
        consumerWithAI.startTimeOut(4000, botWithAI);
    }

    @Override
    public void run() {
        while (true) {
            lockWithAI.lock();
            if (botsWithAI.isEmpty()) {
                try {
                    conditionWithAI.await(); // 自动释放锁
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lockWithAI.unlock();
                    break;
                }
            } else {
                BotWithAI botWithAI = botsWithAI.remove();
                lockWithAI.unlock();
                consumeWithAI(botWithAI); // 比较耗时，放unlock后面
            }
        }
    }
}
