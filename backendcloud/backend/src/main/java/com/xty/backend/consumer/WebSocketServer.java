package com.xty.backend.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.xty.backend.consumer.utils.Game;
import com.xty.backend.consumer.utils.JwtAuthentication;
import com.xty.backend.mapper.RecordMapper;
import com.xty.backend.mapper.UserMapper;
import com.xty.backend.pojo.User;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    public final static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>(); // 将前端建立的每个websocket连接在后端维护起来,对所有实例可见
    private User user;
    private Session session = null; // 维护连接
    private Game game = null;
    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add";
    private final static String removePlayerUrl = "http://127.0.0.1:3001/player/remove";
    // websocket不是标准的spring组件，采取特殊注入方式
    private static UserMapper userMapper;
    public static RecordMapper recordMapper;
    public static RestTemplate restTemplate; // 可以在两个springboot之间通信

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
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
        if (this.user != null) {
            users.remove(this.user.getId());
            // 开始匹配后，刷新当前页面，断开连接，再次匹配，可能会自己和自己匹配，所以要在断开连接时将自己从连接池里删除。
            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("user_id", this.user.getId().toString());
            restTemplate.postForObject(removePlayerUrl, data, String.class);
        }
    }

    public static void startGame(Integer aId, Integer bId) {
        User a = userMapper.selectById(aId), b = userMapper.selectById(bId);

        Game game = new Game(13, 14, 20, a.getId(), b.getId());
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
    }

    private void startMatching() {
        System.out.println("start-matching!");
        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("user_id", this.user.getId().toString());
        req.add("rating", this.user.getRating().toString());
        restTemplate.postForObject(addPlayerUrl, req, String.class);
    }

    private void stopMatching() {
        System.out.println("stop-matching!");
        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("user_id", this.user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, req, String.class);
    }

    private void move(int direction) {
        if (game.getPlayerA().getId().equals(user.getId())) {
            game.setNextStepA(direction);
        } else if (game.getPlayerB().getId().equals(user.getId())) {
            game.setNextStepB(direction);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) { //当做路由 分配任务
        System.out.println("receive message!");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        if ("start-matching".equals(event)) { // 将传来的消息当作路由
            startMatching();
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        } else if ("move".equals(event)) {
            move(data.getInteger("direction"));
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

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
}