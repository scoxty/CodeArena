package com.xty.matchingsystem.service.impl;

import com.xty.matchingsystem.service.MatchingService;
import com.xty.matchingsystem.service.impl.utils.MatchingPool;
import org.springframework.stereotype.Service;

@Service
public class MatchingServiceImpl implements MatchingService {
   public final static MatchingPool matchingpool = new MatchingPool();

    @Override
    public String addPlayer(Integer userId, Integer rating, Integer botId) {
        System.out.println("add player: " + userId + " " + rating);
        matchingpool.addPlayer(userId, rating, botId);
        return "add player success";
    }

    @Override
    public String removePlayer(Integer userId) {
        System.out.println("remove player: " + userId);
        matchingpool.removePlayer(userId);
        return "remove player success";
    }
}
