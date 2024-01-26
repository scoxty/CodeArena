package com.xty.matchingsystem.service.impl.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class MatchingPool {
    private static ScheduledExecutorService sec = Executors.newSingleThreadScheduledExecutor(); // 匹配线程
    private static final int NEED_MATCH_PLAYER_COUNT = 1; // 每个人需要匹配到的玩家数量
    private static ConcurrentHashMap<Integer,MatchPoolPlayerInfo> playerPool = new ConcurrentHashMap<Integer,MatchPoolPlayerInfo>(); // 匹配池，使用ConcurrentHashMap在保证并发安全的同时防止自己匹配自己
    private static RestTemplate restTemplate;
    private final static String startGameUrl = "http://127.0.0.1:3000/pk/start/game";
    private final static String startGameWithAIUrl = "http://127.0.0.1:3000/pkWithAI/start/game";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        MatchingPool.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        // 开启定时任务
        sec.scheduleWithFixedDelay(() -> matchProcess(playerPool), 1,1, TimeUnit.SECONDS);//每隔1秒匹配一次
    }

    public void addPlayerToMatchPool(Integer userId, Integer rating, Integer botId){
        MatchPoolPlayerInfo playerInfo = new MatchPoolPlayerInfo(userId, rating, botId);
        playerPool.put(userId, playerInfo);
    }

    public void removePlayerFromMatchPool(int playerId){
        playerPool.remove(playerId);
    }

    private void matchProcess(ConcurrentHashMap<Integer,MatchPoolPlayerInfo> playerPool) {
        // 首先检查是否有玩家等待时间超过20秒，如果有，则与AI匹配
        for (MatchPoolPlayerInfo player : new ArrayList<>(playerPool.values())) {
            long waitTime = System.currentTimeMillis() - player.getStartMatchTime();
            if (waitTime > 20000) { // 超过20秒
                matchWithAI(player);
            }
        }

        try{
            //先把匹配池中的玩家按分数分布
            TreeMap<Integer, HashSet<MatchPoolPlayerInfo>> pointMap = new TreeMap<Integer,HashSet<MatchPoolPlayerInfo>>();
            for (MatchPoolPlayerInfo matchPlayer : playerPool.values()) {
                //在匹配池中是时间太长，直接移除
                if((System.currentTimeMillis()-matchPlayer.getStartMatchTime())>60 * 60 * 1000){
                    removePlayerFromMatchPool(matchPlayer.getUserId());
                    continue;
                }
                HashSet<MatchPoolPlayerInfo> set = pointMap.get(matchPlayer.getRating());
                if(set==null){
                    set = new HashSet<MatchPoolPlayerInfo>();
                    set.add(matchPlayer);
                    pointMap.put(matchPlayer.getRating(), set);
                }else{
                    set.add(matchPlayer);
                }
            }

            for (HashSet<MatchPoolPlayerInfo> sameRankPlayers: pointMap.values()) {
                boolean continueMatch = true;
                while(continueMatch){
                    //找出同一分数段里，等待时间最长的玩家，用他来匹配，因为他的区间最大
                    //如果他都不能匹配到，等待时间比他短的玩家更匹配不到
                    MatchPoolPlayerInfo oldest = null;
                    for (MatchPoolPlayerInfo playerMatchPoolInfo : sameRankPlayers) {
                        if(oldest==null){
                            oldest = playerMatchPoolInfo;
                        }else if(playerMatchPoolInfo.getStartMatchTime()<oldest.getStartMatchTime()){
                            oldest = playerMatchPoolInfo;
                        }
                    }
                    if(oldest==null){
                        break;
                    }

                    long now = System.currentTimeMillis();
                    int waitSecond = (int)((now-oldest.getStartMatchTime())/1000);

                    //按等待时间扩大匹配范围
                    float c2 = 1.5f;
                    int c3 = 5;
                    int c4 = 1000;

                    float u = (float) Math.pow(waitSecond, c2);
                    u = u + c3;
                    u = (float) Math.round(u);
                    u = Math.min(u, c4);

                    int min = Math.max((oldest.getRating() - (int) u), 0);
                    int max = oldest.getRating() + (int)u;

                    int middle = oldest.getRating();

                    List<MatchPoolPlayerInfo> matchPoolPlayer = new ArrayList<MatchPoolPlayerInfo>();
                    //从中位数向两边扩大范围搜索
                    for(int searchRankUp = middle,searchRankDown = middle; searchRankUp <= max||searchRankDown>=min;searchRankUp++,searchRankDown--){
                        HashSet<MatchPoolPlayerInfo> thisRankPlayers = pointMap.getOrDefault(searchRankUp,new HashSet<MatchPoolPlayerInfo>());
                        if(searchRankDown!=searchRankUp&&searchRankDown>0){
                            thisRankPlayers.addAll(pointMap.getOrDefault(searchRankDown,new HashSet<MatchPoolPlayerInfo>()));
                        }
                        if(!thisRankPlayers.isEmpty()){
                            if(matchPoolPlayer.size()<NEED_MATCH_PLAYER_COUNT){
                                Iterator<MatchPoolPlayerInfo> it = thisRankPlayers.iterator();
                                while (it.hasNext()) {
                                    MatchPoolPlayerInfo player = it.next();
                                    if(!Objects.equals(player.getUserId(), oldest.getUserId())){//排除玩家本身
                                        if(matchPoolPlayer.size()<NEED_MATCH_PLAYER_COUNT){
                                            matchPoolPlayer.add(player);
                                            //移除
                                            it.remove();
                                        }else{
                                            break;
                                        }
                                    }
                                }
                            }else{
                                break;
                            }
                        }
                    }

                    if(matchPoolPlayer.size()==NEED_MATCH_PLAYER_COUNT){
                        //自己也匹配池移除
                        sameRankPlayers.remove(oldest);
                        //匹配成功处理
                        matchPoolPlayer.add(oldest);
                        matchSuccessProcess(matchPoolPlayer);
                    }else{
                        //本分数段等待时间最长的玩家都匹配不到，其他更不用尝试了
                        continueMatch = false;
                        //归还取出来的玩家
                        for(MatchPoolPlayerInfo player:matchPoolPlayer){
                            HashSet<MatchPoolPlayerInfo> sameRankPlayer = pointMap.get(player.getRating());
                            sameRankPlayer.add(player);
                        }
                    }
                }
            }
        }catch(Throwable t){
            System.out.println("匹配发生异常: " + t);
        }
    }

    private void matchWithAI(MatchPoolPlayerInfo player) {
        System.out.println("为id是: " + player.getUserId() + "的用户匹配人机，避免长时间等待");
        removePlayerFromMatchPool(player.getUserId());
        sendResultWithAI(player);
    }

    private void matchSuccessProcess(List<MatchPoolPlayerInfo> matchPoolPlayer) {
        MatchPoolPlayerInfo playerA = matchPoolPlayer.get(0);
        MatchPoolPlayerInfo playerB = matchPoolPlayer.get(1);
        System.out.println(playerA.getUserId() + "和" + playerB.getUserId() + "匹配成功");

        removePlayerFromMatchPool(playerA.getUserId());
        removePlayerFromMatchPool(playerB.getUserId());

        sendResult(playerA, playerB);
    }

    private void sendResult(MatchPoolPlayerInfo a, MatchPoolPlayerInfo b) { // 返回匹配结果
        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("a_id", a.getUserId().toString());
        req.add("a_bot_id", a.getBotId().toString());
        req.add("b_id", b.getUserId().toString());
        req.add("b_bot_id", b.getBotId().toString());
        restTemplate.postForObject(startGameUrl, req, String.class);
    }

    private void sendResultWithAI(MatchPoolPlayerInfo matchPoolPlayerInfo) {
        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("user_id", matchPoolPlayerInfo.getUserId().toString());
        req.add("bot_id", matchPoolPlayerInfo.getBotId().toString());
        restTemplate.postForObject(startGameWithAIUrl, req, String.class);
    }
}
