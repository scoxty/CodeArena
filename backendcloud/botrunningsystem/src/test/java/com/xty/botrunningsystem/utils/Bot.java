package com.xty.botrunningsystem.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Bot implements java.util.function.Supplier<Integer> {
    static class Cell {
        public int x, y;
        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final int SIMULATION_DEPTH = 1000;
    private static final Random random = new Random();

    // 检验当前回合，长度是否增加
    private boolean check_tail_increasing(int step) {
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    // 获取玩家位置
    public List<Cell> getCells(int sx, int sy, String steps) {
        steps = steps.substring(1, steps.length() - 1);
        List<Cell> res = new ArrayList<>();
        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x, y));
        for (int i = 0; i < steps.length(); i++) {
            int d = steps.charAt(i) - '0';
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x, y));
            if (!check_tail_increasing(++step)) {
                res.remove(0);
            }
        }
        return res;
    }

    // MCTS模拟移动
    public Integer simulateMove(int[][] grid, List<Cell> mySnake, List<Cell> opponentSnake) {
        int bestMove = -1;
        int bestScore = -1;

        for (int i = 0; i < 4; i++) {
            int score = 0;
            for (int j = 0; j < SIMULATION_DEPTH; j++) {
                score += simulateOneGame(grid, mySnake, opponentSnake, i);
            }
            if (score > bestScore) {
                bestScore = score;
                bestMove = i;
            }
        }
        return bestMove;
    }

    // 进行一次模拟游戏
    public int simulateOneGame(int[][] grid, List<Cell> mySnake, List<Cell> opponentSnake, int initialMove) {
        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int myScore = 0;

        List<Cell> simulatedMySnake = new ArrayList<>(mySnake);
        List<Cell> simulatedOpponentSnake = new ArrayList<>(opponentSnake);
        int step = mySnake.size();

        // 首次移动
        moveSnake(simulatedMySnake, dx[initialMove], dy[initialMove], step);

        while (true) {
            // 随机移动
            int myMove = random.nextInt(4);
            int opponentMove = random.nextInt(4);

            moveSnake(simulatedMySnake, dx[myMove], dy[myMove], step);
            moveSnake(simulatedOpponentSnake, dx[opponentMove], dy[opponentMove], step);

            // 检查是否碰撞
            Cell myHead = simulatedMySnake.get(simulatedMySnake.size() - 1);
            if (myHead.x < 0 || myHead.x >= 13 || myHead.y < 0 || myHead.y >= 14 || grid[myHead.x][myHead.y] == 1) {
                break;
            }

            myScore++;
        }
        return myScore;
    }

    // 移动蛇
    private void moveSnake(List<Cell> snake, int dx, int dy, int step) {
        Cell head = snake.get(snake.size() - 1);
        snake.add(new Cell(head.x + dx, head.y + dy));
        if (!check_tail_increasing(step)) {
            snake.remove(0);
        }
    }

    // 主要决策函数
    public Integer nextMove(String input) {
        String[] strs = input.split("#");
        int[][] g = new int[13][14];
        for (int i = 0, k = 0; i < 13; i++) {
            for (int j = 0; j < 14; j++, k++) {
                if (strs[0].charAt(k) == '1') {
                    g[i][j] = 1;
                }
            }
        }

        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);
        int bSx = Integer.parseInt(strs[4]), bSy = Integer.parseInt(strs[5]);

        List<Cell> aCells = getCells(aSx, aSy, strs[3]);
        List<Cell> bCells = getCells(bSx, bSy, strs[6]);

        for (Cell c: aCells) g[c.x][c.y] = 1;
        for (Cell c: bCells) g[c.x][c.y] = 1;

        return simulateMove(g, aCells, bCells);
    }

    @Override
    public Integer get() {
        File file = new File("input.txt");
        try {
            Scanner sc = new Scanner(file);
            return nextMove(sc.next());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
