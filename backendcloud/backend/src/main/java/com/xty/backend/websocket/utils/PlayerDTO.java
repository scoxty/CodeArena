package com.xty.backend.websocket.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class PlayerDTO {
    public Integer userId;
    public Integer botId;
    public Integer sx;
    public Integer sy;
    public List<Integer> steps;

    public PlayerDTO(Player player) {
        this.userId = player.getId();
        this.botId = player.getBotId();
        this.sx = player.getSx();
        this.sy = player.getSy();
        this.steps = player.getSteps();
    }

    // 无参构造函数
    public PlayerDTO() {
    }

    // setter方法
    public void setUserId(Integer userId) { this.userId = userId; }
    public void setBotId(Integer botId) { this.botId = botId; }
    public void setSx(Integer sx) { this.sx = sx; }
    public void setSy(Integer sy) { this.sy = sy; }
    public void setSteps(List<Integer> steps) { this.steps = steps; }
}
