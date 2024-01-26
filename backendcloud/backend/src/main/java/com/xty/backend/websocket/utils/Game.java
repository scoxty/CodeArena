package com.xty.backend.websocket.utils;

import com.alibaba.fastjson2.JSONObject;
import com.xty.backend.websocket.WebSocketServer;
import com.xty.backend.pojo.Bot;
import com.xty.backend.pojo.Record;
import com.xty.backend.pojo.User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread {
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;
    private final int[][] g; // 地图
    private final static int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
    private final Player playerA, playerB;
    private Integer nextStepA;
    private Integer nextStepB;
    private ReentrantLock lock = new ReentrantLock();
    private String status = "playing"; // playing -> finished
    private String loser = ""; // all: 平局, A: A输, B: B输
    private static final int K = 30; // Elo机制的K因子，可以根据需要调整
    private final static String addBotUrl = "http://127.0.0.1:3002/bot/add";
    private final static String addBotWithAIUrl = "http://127.0.0.1:3002/botWithAI/add";

    public Game(Integer rows, Integer cols, Integer inner_walls_count, Integer idA, Bot botA, Integer idB, Bot botB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];

        Integer botIdA = -1, botIdB = -1;
        String botCodeA = "", botCodeB = "";
        if (botA != null) {
            botIdA = botA.getId();
            botCodeA = botA.getContent();
        }
        if (botB != null) {
            botIdB = botB.getId();
            botCodeB = botB.getContent();
        }

        playerA = new Player(idA, botIdA, botCodeA, rows - 2, 1, new ArrayList<>());
        playerB = new Player(idB, botIdB, botCodeB, 1, cols - 2, new ArrayList<>());
    }

    public int[][] getG() {
        return g;
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();
        }
    }

    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }

    private boolean check_connectivity(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) {
            return true;
        }

        g[sx][sy] = 1;

        for (int i = 0; i < 4; i ++) {
            int x = sx + dx[i], y = sy + dy[i];
            if (x >= 0 && x < this.rows && y >= 0 && y < this.cols && g[x][y] == 0) {
                g[sx][sy] = 0;
                return true;
            }
        }

        g[sx][sy] = 0;
        return false;
    }

    private boolean draw() {
        // 标记障碍物
        for (int i = 0; i < this.rows; i ++) {
            for (int j = 0; j < this.cols; j ++) {
                g[i][j] = 0; //0表示可通行区域 1表示障碍物
            }
        }

        // 给四周添加障碍物
        for (int r = 0; r < this.rows; r ++) {
            g[r][0] = g[r][this.cols - 1] = 1;
        }
        for (int c = 0; c < this.cols; c ++) {
            g[0][c] = g[this.rows - 1][c] = 1;
        }

        // 创建内部随机障碍物
        // 保证双方公平，按中心对称的方式放置。
        Random random = new Random();
        for (int i = 0; i < this.inner_walls_count / 2; i ++) {
            for (int j = 0; j < 1000; j ++) {
                int r = random.nextInt(this.rows);
                int c = random.nextInt(this.cols);

                if (g[r][c] == 1 || g[this.rows - 1 - r][this.cols - 1 - c] == 1) {
                    continue;
                }

                // 左下角和右上角为玩家起点，不能有障碍物
                if (r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2) {
                    continue;
                }

                g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = 1;
                break;
            }
        }

        return check_connectivity(this.rows - 2, 1, 1, this.cols - 2);
    }

    public void createMap() {
        for (int i = 0; i < 1000; i ++) {
            if (draw()) {
                break;
            }
        }
    }

    // 将当前局面信息编码成字符串
    // 地图#me.sx#me.sy#我的操作#you.sx#you.sy#你的操作
    private String getInput(Player player) {
        Player me, you;
        if (playerA.getId().equals(player.getId())) {
            me = playerA;
            you = playerB;
        } else {
            me = playerB;
            you = playerA;
        }

        return getMapString() + "#" +
                me.getSx() + "#" +
                me.getSy() + "#(" +
                me.getStepString() + ")#" +
                you.getSx() + "#" +
                you.getSy() + "#(" +
                you.getStepString() + ")";
    }

    private void sendBotCode(Player player) {
        if (player.getBotId().equals(-1)) { // 亲自出马，不需要执行代码。
            return;
        }

        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("user_id", player.getId().toString());
        req.add("bot_code", player.getBotCode());
        req.add("input", getInput(player));

        WebSocketServer.restTemplate.postForObject(addBotUrl, req, String.class);
    }

    private void sendBotCodeWithAI() {
        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();

        req.add("user_id", playerA.getId().toString());
        if (playerA.getBotId().equals(-1)) {
            req.add("bot_code", "");
            req.add("input", "");
        } else {
            req.add("bot_code", playerA.getBotCode());
            req.add("input", getInput(playerA));
        }
        req.add("ai_id", playerB.getId().toString());
        req.add("ai_bot_code", playerB.getBotCode());
        req.add("input2", getInput(playerB));

        WebSocketServer.restTemplate.postForObject(addBotWithAIUrl, req, String.class);
    }

    private boolean nextStep() { // 等待两名玩家下一步操作
        //由于前端动画200ms才能画一个格子
        //如果在此期间接收到的输入多于一步 只会留最后一步 多余的会被覆盖
        //因此在每一个下一步都要先休息200ms
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (playerB.getId() != WebSocketServer.AI.getId()) {
            sendBotCode(playerA);
            sendBotCode(playerB);
        } else {
            sendBotCodeWithAI();
        }

        try {
            for (int i = 0; i < 50; i ++) {
                Thread.sleep(100);
                lock.lock();
                try {
                    if (nextStepA != null && nextStepB != null) {
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("超过5s依旧没有成功获取两名玩家下一步操作");

        return false;
    }

    private void sendAllMessage(String message) {
        if (WebSocketServer.users.get(playerA.getId()) != null) {
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        }
        if (WebSocketServer.users.get(playerB.getId()) != null) {
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
        }
    }

    private String getMapString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i ++) {
            for (int j = 0; j < cols; j ++) {
                res.append(g[i][j]);
            }
        }
        return res.toString();
    }

    private void updateUserRating(Player player, Integer rating) {
        User user = WebSocketServer.userMapper.selectById(player.getId());
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }

    private double calculateExpectedScore(int rating1, int rating2) {
        return 1.0 / (1.0 + Math.pow(10, (rating2 - rating1) / 400.0));
    }

    private void saveToDatabase() {
        int ratingA = WebSocketServer.userMapper.selectById(playerA.getId()).getRating();
        int ratingB = WebSocketServer.userMapper.selectById(playerB.getId()).getRating();

        double expectedScoreA = calculateExpectedScore(ratingA, ratingB);
        double expectedScoreB = calculateExpectedScore(ratingB, ratingA);

        double scoreA, scoreB;

        if ("A".equals(loser)) {
            scoreA = 0; // A 输了
            scoreB = 1; // B 赢了
        } else if ("B".equals(loser)) {
            scoreA = 1; // A 赢了
            scoreB = 0; // B 输了
        } else { // 平局情况
            scoreA = 0.5;
            scoreB = 0.5;
        }

        int newRatingA = ratingA + (int)(K * (scoreA - expectedScoreA));
        int newRatingB = ratingB + (int)(K * (scoreB - expectedScoreB));

        updateUserRating(playerA, newRatingA);
        updateUserRating(playerB, newRatingB);

        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepString(),
                playerB.getStepString(),
                getMapString(),
                loser,
                new Date()
        );

        WebSocketServer.recordMapper.insert(record);
    }

    // 向两个Client公布结果
    private void sendResult() {
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveToDatabase();
        sendAllMessage(resp.toJSONString());
    }

    private void sendMove() {
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            nextStepA = nextStepB = null;
            sendAllMessage(resp.toJSONString());

        } finally {
            lock.unlock();
        }
    }

    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB) {
        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1); // 取出蛇头
        // 是否撞墙
        if (g[cell.x][cell.y] == 1) {
            return false;
        }

        // 是否撞到自己
        for (int i = 0; i < n - 1; i ++) {
            if (cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y) {
                return false;
            }
        }

        // 是否撞到别人
        for (int i = 0; i < n - 1; i ++) {
            if (cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y) {
                return false;
            }
        }

        return true;
    }

    // 判断两名玩家下一步操作是否合法
    private void judge() {
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if (!validA || !validB) {
            status = "finished";

            if (!validA && !validB) {
                loser = "all";
            } else if (!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i ++) { //1000步之内游戏肯定结束
            if (nextStep()) { // 是否获取了两条蛇的下一步操作
                System.out.println("获取到两条蛇下一步动作");
                judge();
                if (status.equals("playing")) {
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else {
                status = "finished";
                lock.lock();
                try{
                    if (nextStepA == null && nextStepB == null) {
                        loser = "all";
                    } else if (nextStepA == null) {
                        loser = "A";
                    } else {
                        loser = "B";
                    }
                } finally {
                    lock.unlock();
                }

                sendResult();
                break;
            }
        }
    }
}
