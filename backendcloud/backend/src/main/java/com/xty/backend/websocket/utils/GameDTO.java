package com.xty.backend.websocket.utils;

import java.util.Arrays;
import java.util.List;

public class GameDTO {
    public Integer rows;
    public Integer cols;
    public Integer inner_walls_count;
    public int[][] g; // 地图
    public PlayerDTO playerDTOA, playerDTOB;

    public GameDTO(Integer rows, Integer cols, Integer inner_walls_count, int[][] g, Player playerA, Player playerB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = g;
        this.playerDTOA = new PlayerDTO(playerA);
        this.playerDTOB = new PlayerDTO(playerB);
    }

    // 无参构造函数
    public GameDTO() {
    }

    // 提供setter方法以便反序列化后设置值
    public void setRows(Integer rows) { this.rows = rows; }
    public void setCols(Integer cols) { this.cols = cols; }
    public void setInner_walls_count(Integer inner_walls_count) { this.inner_walls_count = inner_walls_count; }
    public void setG(int[][] g) { this.g = g; }
    public void setPlayerDTOA(PlayerDTO playerDTOA) { this.playerDTOA = playerDTOA; }
    public void setPlayerDTOB(PlayerDTO playerDTOB) { this.playerDTOB = playerDTOB; }
}
