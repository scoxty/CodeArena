import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AIBot implements java.util.function.Supplier<Integer>{
    @Override
    public Integer get() {
        File file = new File("./AI_Input.txt");
        try {
            Scanner sc = new Scanner(file);
            return nextMove(sc.next());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static class Cell {
        public int x, y;
        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // 检验当前回合，长度是否增加  true 增加, 增加时-头部移动,尾部不变, 不增加-头部移动,尾部删除
    private static boolean check_tail_increasing(int step) {
        if (step <= 10) return true; // 前10回合每回合长度+1
        return step % 3 == 1; //10回合之后每三回合长度+1
    }

    // 根据收集的玩家操作, 计算并返回玩家的位置
    public static List<Cell> getCells(int sx, int sy, String steps) {
        steps = steps.substring(1, steps.length() - 1);
        List<Cell> res = new ArrayList<>();

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x, y));
        for (int i = 0; i < steps.length(); i ++ ) {
            int d = steps.charAt(i) - '0';
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x, y));
            if (!check_tail_increasing( ++ step)) {
                res.remove(0);
            }
        }
        return res;
    }

    // 传入的参数记录了如下信息：
    // 地图#自己起始横坐标#自己起始纵坐标#(自己操作)#对手起始横坐标#对手起始纵坐标#(对手操作)
    public static Integer nextMove(String input) {
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

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int bestMove = -1;
        int maxSpace = -1;

        for (int i = 0; i < 4; i++) {
            int x = aCells.get(aCells.size() - 1).x + dx[i];
            int y = aCells.get(aCells.size() - 1).y + dy[i];
            if (x >= 0 && x < 13 && y >= 0 && y < 14 && g[x][y] == 0) {
                int space = calculateSpace(g, x, y);
                if (space > maxSpace) {
                    maxSpace = space;
                    bestMove = i;
                }
            }
        }

        return bestMove >= 0 ? bestMove : 0; // 如果没有合法移动，随便返回一个方向
    }

    // 计算在特定位置可用的空间大小
    private static int calculateSpace(int[][] g, int x, int y) {
        boolean[][] visited = new boolean[13][14];
        return dfs(g, visited, x, y);
    }

    // 使用深度优先搜索（DFS）来计算空间
    private static int dfs(int[][] g, boolean[][] visited, int x, int y) {
        if (x < 0 || x >= 13 || y < 0 || y >= 14 || g[x][y] == 1 || visited[x][y]) {
            return 0;
        }
        visited[x][y] = true;
        return 1 + dfs(g, visited, x + 1, y) + dfs(g, visited, x - 1, y)
                + dfs(g, visited, x, y + 1) + dfs(g, visited, x, y - 1);
    }
}