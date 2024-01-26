package com.xty.botrunningsystem.service.impl.utils;

import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class ConsumerWithAI extends Thread{
    private BotWithAI botWithAI;
    private static RestTemplate restTemplate;
    private final static String receiveBotMoveUrl = "http://127.0.0.1:3000/pkWithAI/receive/bot/move";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        ConsumerWithAI.restTemplate = restTemplate;
    }

    public void startTimeOut(long timeout, BotWithAI botWithAI) {
        this.botWithAI = botWithAI;
        this.start();

        try {
            this.join(timeout); // 最多等到timeout毫秒秒。可提前结束，比sleep好
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.interrupt(); // 中断当前线程
        }
    }

    // 在code中的类名后添加uid
    private String addUid(String code, String uid) {
        int k = code.indexOf(" implements java.util.function.Supplier<Integer>");
        return code.substring(0, k) + uid + code.substring(k);
    }

    @Override
    public void run() {
        Integer directionA = -1;
        if (!Objects.equals(botWithAI.botCode, "")) {
            directionA = compileAndExecuteCode(botWithAI.botCode, botWithAI.input);
        }
        Integer directionB = compileAndExecuteCode(botWithAI.aiBotCode, botWithAI.input2);

        if (!Objects.equals(botWithAI.botCode, "")) {
            System.out.println("id为:" + botWithAI.userId + "的用户的move-direction: " + directionA);
        }
        System.out.println("AI的move-direction: " + botWithAI.aiId + " " + directionB);

        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("user_id_a", botWithAI.userId.toString());
        req.add("direction_a", directionA.toString());
        req.add("user_id_b", botWithAI.aiId.toString());
        req.add("direction_b", directionB.toString());

        restTemplate.postForObject(receiveBotMoveUrl, req, String.class);
    }

    private Integer compileAndExecuteCode(String botCode, String input) {
        // 编译执行用户的代码：
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0, 8);
        // 相同类名只会编译一次，为了处理每次输入，可在类名后拼接一个随机字符串。
        Supplier<Integer> botInterface = Reflect.compile(
                "com.codearena.botrunningsystem.utils.Bot" + uid,
                addUid(botCode, uid)
        ).create().get();

        File file = new File("input.txt");
        try (PrintStream fout = new PrintStream(file)) {
            fout.println(input);
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return botInterface.get();
    }
}
