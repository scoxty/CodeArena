package com.xty.matchingsystem.service.impl;

import com.xty.matchingsystem.service.MatchingService;
import com.xty.matchingsystem.service.impl.utils.MatchingPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MatchingServiceImpl implements MatchingService {
    @Autowired
    private MatchingPool matchingPool;

    @Override
    public String addPlayer(Integer userId, Integer rating, Integer botId) {
        System.out.println("add player: " + userId + " " + rating);
        matchingPool.addPlayerToMatchPool(userId, rating, botId);
        return "add player success";
    }

    @Override
    public String removePlayer(Integer userId) {
        System.out.println("remove player: " + userId);
        matchingPool.removePlayerFromMatchPool(userId);
        return "remove player success";
    }
}
