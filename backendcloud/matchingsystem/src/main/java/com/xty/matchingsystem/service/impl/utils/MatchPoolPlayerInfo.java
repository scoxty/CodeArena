package com.xty.matchingsystem.service.impl.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPoolPlayerInfo {
    private Integer userId;
    private Integer botId;
    private Integer rating;
    private Long startMatchTime;//开始匹配时间

    public MatchPoolPlayerInfo(Integer userId, Integer rating, Integer botId) {
        this.userId = userId;
        this.botId = botId;
        this.rating = rating;
        this.startMatchTime = System.currentTimeMillis();
    }
}
