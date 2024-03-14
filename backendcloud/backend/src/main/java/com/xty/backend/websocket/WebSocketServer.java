package com.xty.backend.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xty.backend.service.impl.user.account.RegisterServiceImpl;
import com.xty.backend.websocket.utils.Game;
import com.xty.backend.websocket.utils.GameDTO;
import com.xty.backend.websocket.utils.JwtAuthentication;
import com.xty.backend.mapper.BotMapper;
import com.xty.backend.mapper.RecordMapper;
import com.xty.backend.mapper.UserMapper;
import com.xty.backend.pojo.Bot;
import com.xty.backend.pojo.User;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    public final static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>(); // 将前端建立的每个websocket连接在后端维护起来,对所有实例可见
    private Session session = null; // 维护连接
    // 心跳机制
    private static final long HEARTBEAT_INTERVAL = 35000; // 客户端是30秒发送一次，服务器稍大一些
    private static final ConcurrentHashMap<Session, Long> lastHeartbeatReceived = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService sec = Executors.newSingleThreadScheduledExecutor();

    private User user;
    public static User AI;
    private static List<Bot> aiBots;
    public Game game = null;

    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add";
    private final static String removePlayerUrl = "http://127.0.0.1:3001/player/remove";
    public static RestTemplate restTemplate; // 可以在两个springboot之间通信

    // websocket不是标准的spring组件，采取特殊注入方式
    public static UserMapper userMapper;
    public static RecordMapper recordMapper;
    private static BotMapper botMapper;
    private static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        WebSocketServer.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setBotMapper(BotMapper botMapper) {
        WebSocketServer.botMapper = botMapper;
    }

    @PostConstruct
    public void init() {
        // 初始化AI
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(User::getUsername, "代码幽灵");
        AI = userMapper.selectOne(userQueryWrapper);
        LambdaQueryWrapper<Bot> botQueryWrapper = new LambdaQueryWrapper<>();
        botQueryWrapper.eq(Bot::getUserId, AI.getId());
        aiBots = botMapper.selectList(botQueryWrapper);

        // 开启定时任务，每10秒检测一次心跳
        sec.scheduleWithFixedDelay(this::checkHeartbeat, 10, 10, TimeUnit.SECONDS);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        this.session = session;
        System.out.println("connected!" + this);
        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);
        if (this.user != null) {
            users.put(userId, this);
        } else {
            this.session.close();
        }
    }

    @OnClose
    public void onClose() {
        System.out.println("disconnected!");
        // 将user从users中删去
        // 检查当前关闭的session是否仍是users中对应的session
        WebSocketServer current = users.get(this.user.getId());
        if (this.user != null && current == this) { // 确保是相同的WebSocketServer实例
            users.remove(this.user.getId());
            // 开始匹配后，刷新当前页面，断开连接，断开连接时将自己从连接池里删除。
            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("user_id", this.user.getId().toString());
            restTemplate.postForObject(removePlayerUrl, data, String.class);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) { //当做路由 分配任务
        System.out.println("receive message!");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        if ("ping".equals(event)) {
            pong();
        } else if ("synchronize-data".equals(event)) {
            synchronizeData();
        } else if ("start-matching".equals(event)) { // 将传来的消息当作路由
            startMatching(data.getInteger("bot_id"));
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        } else if ("move".equals(event)) {
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public static void startGameWithAI(Integer userId, Integer botId) {
        User user = userMapper.selectById(userId);
        Bot bot = botMapper.selectById(botId);
        Random random = new Random();
        int idx = random.nextInt(4); // 选择AI参战的Bot

        Game game = new Game(13, 14, 20, user.getId(), bot, AI.getId(), aiBots.get(idx));
        game.createMap();

        // 防止用户连接突然断开导致空指针异常
        if (users.get(user.getId()) != null) {
            users.get(user.getId()).game = game;
        }
        game.start();

        JSONObject respGame = new JSONObject();
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getG());
        JSONObject resp = new JSONObject();
        resp.put("event", "start-matching");
        resp.put("opponent_username", AI.getUsername());
        resp.put("opponent_photo", AI.getPhoto());
        resp.put("game", respGame);
        if (users.get(user.getId()) != null) {
            users.get(user.getId()).sendMessage(resp.toJSONString());
        }

        GameDTO gameDTO = new GameDTO(
                game.getRows(),
                game.getCols(),
                game.getInner_walls_count(),
                game.getG(),
                game.getPlayerA(),
                game.getPlayerB());
        saveGameToRedis(userId, gameDTO);
    }

    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId) {
        User a = userMapper.selectById(aId), b = userMapper.selectById(bId);
        Bot botA = botMapper.selectById(aBotId), botB = botMapper.selectById(bBotId);

        Game game = new Game(13, 14, 20, a.getId(), botA, b.getId(), botB);
        game.createMap();

        // 防止用户连接突然断开导致空指针异常
        if (users.get(a.getId()) != null) {
            users.get(a.getId()).game = game;
        }
        if (users.get(b.getId()) != null) {
            users.get(b.getId()).game = game;
        }
        game.start();

        JSONObject respGame = new JSONObject();
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getG());

        JSONObject respA = new JSONObject();
        respA.put("event", "start-matching");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game", respGame);
        if (users.get(a.getId()) != null) {
            users.get(a.getId()).sendMessage(respA.toJSONString());
        }

        JSONObject respB = new JSONObject();
        respB.put("event", "start-matching");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game", respGame);
        if (users.get(b.getId()) != null) {
            users.get(b.getId()).sendMessage(respB.toJSONString());
        }

        GameDTO gameDTO = new GameDTO(
                game.getRows(),
                game.getCols(),
                game.getInner_walls_count(),
                game.getG(),
                game.getPlayerA(),
                game.getPlayerB());
        saveGameToRedis(aId, gameDTO);
        saveGameToRedis(bId, gameDTO);
    }

    public static void saveGameToRedis(Integer userId, GameDTO gameDTO) {
        // 过期时间: 最迟20s匹配 + 每轮(200ms前端渲染 + 5s bot代码执行) * 523轮（理论最大值）≈ 46 min, 实际耗时比这个少。
        redisTemplate.opsForValue().set("pk:" + userId, JSON.toJSONString(gameDTO), 46, TimeUnit.MINUTES);
    }

    public static void delGameFromRedis(Integer userId) {
        redisTemplate.delete("pk:" + userId);
    }

    private void startMatching(Integer botId) {
        System.out.println("start-matching!");
        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("user_id", this.user.getId().toString());
        req.add("rating", this.user.getRating().toString());
        req.add("bot_id", botId.toString());
        restTemplate.postForObject(addPlayerUrl, req, String.class);
    }

    private void stopMatching() {
        System.out.println("stop-matching!");
        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("user_id", this.user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, req, String.class);
    }

    private void move(int direction) {
        if (game.getPlayerA().getId().equals(user.getId()) && game.getPlayerA().getBotId().equals(-1)) {
            game.setNextStepA(direction);
        } else if (game.getPlayerB().getId().equals(user.getId()) && game.getPlayerB().getBotId().equals(-1)) {
            game.setNextStepB(direction);
        }
    }

    private void pong() {
        // 更新该Session的最后心跳时间
        lastHeartbeatReceived.put(this.session, System.currentTimeMillis());
        JSONObject resp = new JSONObject();
        resp.put("event", "pong");
        this.sendMessage(resp.toJSONString());
    }

    private void checkHeartbeat() {
        long now = System.currentTimeMillis();
        // 收集所有超时的会话
        Set<Session> sessionsToRemove = lastHeartbeatReceived.entrySet().stream()
                .filter(entry -> (now - entry.getValue()) > HEARTBEAT_INTERVAL)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // 关闭和移除所有超时的会话
        for (Session session : sessionsToRemove) {
            try {
                if (session.isOpen()) { // 检查会话是否仍然开放
                    session.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (!session.isOpen()) {
                    lastHeartbeatReceived.remove(session);
                }
            }
        }
    }

    private void synchronizeData() {
        Object value = redisTemplate.opsForValue().get("pk:" + this.user.getId());
        if (value != null) { // redis中还保存对局数据说明游戏还没结束
            // 同步服务端
            String s = (String)value;
            GameDTO gameDTO = JSON.parseObject(s, GameDTO.class);
            Bot botA = botMapper.selectById(gameDTO.playerDTOA.botId), botB = botMapper.selectById(gameDTO.playerDTOB.botId);
            Game game = new Game(gameDTO, botA, botB);

            if (users.get(game.getPlayerA().getId()) != null) {
                if (users.get(game.getPlayerA().getId()).game != null) { // 中断之前的游戏线程
                    users.get(game.getPlayerA().getId()).game.interrupt();
                }
                users.get(game.getPlayerA().getId()).game = game;
            }
            if (users.get(game.getPlayerB().getId()) != null) {
                if (users.get(game.getPlayerB().getId()).game != null) { // 中断之前的游戏线程
                    users.get(game.getPlayerB().getId()).game.interrupt();
                }
                users.get(game.getPlayerB().getId()).game = game;
            }
            // 开启新的游戏线程
            game.start();
            // 同步前端
            JSONObject respGame = new JSONObject();
            respGame.put("a_id", game.getPlayerA().getId());
            respGame.put("a_sx", game.getPlayerA().getSx());
            respGame.put("a_sy", game.getPlayerA().getSy());
            respGame.put("b_id", game.getPlayerB().getId());
            respGame.put("b_sx", game.getPlayerB().getSx());
            respGame.put("b_sy", game.getPlayerB().getSy());
            respGame.put("map", game.getG());
            JSONObject resp = new JSONObject();
            resp.put("event", "synchronize-data");
            resp.put("game", respGame);
            this.sendMessage(resp.toJSONString());
        }
    }

    public void sendMessage(String message) { // 从后端到前端
        synchronized (this.session) { // 异步通信，上锁
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}