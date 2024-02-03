package com.xty.botrunningsystem.service.impl.utils;

import com.xty.botrunningsystem.service.impl.utils.sandbox.CodeExecutionStrategy;
import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Component
public class Consumer {
    private static RestTemplate restTemplate;
    private final static String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move";
    private final static String receiveBotMoveWithAIUrl = "http://127.0.0.1:3000/pkWithAI/receive/bot/move";

    @Autowired
    private ApplicationContext applicationContext; // 用于获取策略实现

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    public void startTimeOut(long timeout, Bot bot) {
        CompletableFuture<Void> future = processBotAsync(bot);
        try {
            future.get(timeout, TimeUnit.MILLISECONDS); // 等待直到异步任务完成或超时
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true); // 如果发生超时或中断，取消异步任务
            e.printStackTrace();
        }
    }

    @Async
    public CompletableFuture<Void> processBotAsync(Bot bot) {
        try {
            if (bot.getAiId() == null) {
                consumeWithoutAI(bot);
            } else {
                consumeWithAI(bot);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    private void consumeWithoutAI(Bot bot) {
        // 编译运行玩家的代码
        String language = bot.getType();
        CodeExecutionStrategy strategy = applicationContext.getBean(language + "ExecutionStrategy", CodeExecutionStrategy.class);

        Integer direction = strategy.executeCode(bot.getBotCode(), bot.getInput());

        System.out.println("move-direction: " + bot.getUserId() + " " + direction);

        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("user_id", bot.getUserId().toString());
        req.add("direction", direction.toString());

        restTemplate.postForObject(receiveBotMoveUrl, req, String.class);
    }

    private void consumeWithAI(Bot bot) {
        // 用户的代码放到docker执行
        CompletableFuture<Integer> playerDirectionFuture = CompletableFuture.supplyAsync(() -> {
            if (!Objects.equals(bot.getBotCode(), "")) { // 玩家的Bot参战
                CodeExecutionStrategy strategy = applicationContext.getBean(bot.getType() + "ExecutionStrategy", CodeExecutionStrategy.class);
                return strategy.executeCode(bot.getBotCode(), bot.getInput());
            }
            return -1; // 如果玩家自己参战，则返回-1
        });

        // 人机的代码默认Java且信任度较高，直接使用joor动态编译执行
        CompletableFuture<Integer> aiDirectionFuture = CompletableFuture.supplyAsync(() -> compileAndExecuteCode(bot.getAiBotCode(), bot.getInput2()));

        CompletableFuture.allOf(playerDirectionFuture, aiDirectionFuture).join(); // 等待所有的Future完成

        // 获取Future的结果
        Integer directionA = playerDirectionFuture.join();
        Integer directionB = aiDirectionFuture.join();

        if (!Objects.equals(bot.getBotCode(), "")) {
            System.out.println("id为:" + bot.getUserId() + "的用户的move-direction: " + directionA);
        }
        System.out.println("AI的move-direction: " + bot.getAiId() + " " + directionB);

        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("user_id_a", bot.getUserId().toString());
        req.add("direction_a", directionA.toString());
        req.add("user_id_b", bot.getAiId().toString());
        req.add("direction_b", directionB.toString());

        restTemplate.postForObject(receiveBotMoveWithAIUrl, req, String.class);
    }

    private Integer compileAndExecuteCode(String botCode, String input) {
        // 编译执行AI的代码：
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0, 8);
        // 相同类名只会编译一次，为了处理每次输入，可在类名后拼接一个随机字符串。
        Supplier<Integer> botInterface = Reflect.compile(
                "AIBot" + uid,
                addUid(botCode, uid)
        ).create().get();

        File file = new File("AI_Input.txt");
        try (PrintStream fout = new PrintStream(file)) {
            fout.println(input);
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return botInterface.get();
    }

    // 在code中的类名后添加uid
    private String addUid(String code, String uid) {
        int k = code.indexOf(" implements java.util.function.Supplier<Integer>");
        return code.substring(0, k) + uid + code.substring(k);
    }

}
