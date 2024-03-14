package com.xty.botrunningsystem.service.impl.utils;

import com.xty.botrunningsystem.service.impl.utils.sandbox.CodeExecutionStrategy;
import jakarta.annotation.PreDestroy;
import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.concurrent.*;
import java.util.function.Supplier;

@Component
public class Consumer {
    private static RestTemplate restTemplate;
    private final static String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move";
    private final static String receiveBotMoveWithAIUrl = "http://127.0.0.1:3000/pkWithAI/receive/bot/move";

    @Autowired
    private ApplicationContext applicationContext; // 用于获取策略实现

    @Autowired
    private ThreadPoolExecutor sandboxExecutor; // 用于管理代码沙箱线程的线程池

    @Autowired
    private ThreadPoolExecutor botConsumeExecutor; // 用于管理bot消费端线程

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    public void consumeBot(Bot bot) {
        botConsumeExecutor.submit(()->{
            runBotCode(bot);
        });
    }

    public void runBotCode(Bot bot) {
        if (bot.getAiId() == null) {
            consumeWithoutAI(bot);
        } else {
            consumeWithAI(bot);
        }
    }

    private void consumeWithoutAI(Bot bot) {
        // 编译运行玩家的代码
        String language = bot.getType();
        CodeExecutionStrategy strategy = applicationContext.getBean(language + "ExecutionStrategy", CodeExecutionStrategy.class);

        long pre = System.currentTimeMillis();
        Integer direction = strategy.executeCode(bot.getUserId(), bot.getBotCode(), bot.getInput());
        long now = System.currentTimeMillis();
        System.out.println("docker代码沙箱的总执行时间: " + (now - pre) + "ms");
        System.out.println("move-direction: " + bot.getUserId() + " " + direction);

        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("user_id", bot.getUserId().toString());
        req.add("direction", direction.toString());

        restTemplate.postForObject(receiveBotMoveUrl, req, String.class);
    }

    private void consumeWithAI(Bot bot) {
        long last = System.currentTimeMillis();

        // 用户的代码放到docker执行
        Future<Integer> playerDirectionFuture = botConsumeExecutor.submit(() -> {
            if (!Objects.equals(bot.getBotCode(), "")) { // 玩家的Bot参战
                CodeExecutionStrategy strategy = applicationContext.getBean(bot.getType() + "ExecutionStrategy", CodeExecutionStrategy.class);
                return strategy.executeCode(bot.getUserId(), bot.getBotCode(), bot.getInput());
            }
            return -1; // 如果玩家自己参战，则返回-1
        });

        // 人机的代码默认Java且信任度较高，直接使用joor动态编译执行
        Future<Integer> aiDirectionFuture = botConsumeExecutor.submit(() -> compileAndExecuteCode(bot.getAiBotCode(), bot.getInput2()));

        // 获取Future的结果
        Integer directionA = null, directionB = null;
        try {
            directionA = playerDirectionFuture.get();
            directionB = aiDirectionFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (!Objects.equals(bot.getBotCode(), "")) {
            System.out.println("id为:" + bot.getUserId() + "的用户的move-direction: " + directionA);
        }
        System.out.println("AI的move-direction: " + bot.getAiId() + " " + directionB);

        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("user_id_a", bot.getUserId().toString());
        req.add("direction_a", directionA.toString());
        req.add("user_id_b", bot.getAiId().toString());
        req.add("direction_b", directionB.toString());

        long now = System.currentTimeMillis();
        System.out.println("用户+AI代码执行总耗时: " + (now - last) + "ms");

        restTemplate.postForObject(receiveBotMoveWithAIUrl, req, String.class);
    }

    // 由于AI有服务端提供，是可信任的并且是Java代码，因此直接使用supplier编译
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

    @PreDestroy
    public void destroy() {
        // 清理线程池资源
        shutdownAndAwaitTermination(botConsumeExecutor);
        shutdownAndAwaitTermination(sandboxExecutor);
    }

    private void shutdownAndAwaitTermination(ThreadPoolExecutor pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
    }
}
