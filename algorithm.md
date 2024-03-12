# Bot的算法

下面只是列举几个算法，欢迎补充~

### 暴力法
### 介绍
这个算法的核心是简单地避免撞到障碍物（包括自身和对手的蛇身）。

它首先获取蛇当前的位置和可能的移动方向，然后检查每个方向是否有障碍物。如果发现可移动的方向，它就选择这个方向。

这个算法比较简单，适用于基础的避免碰撞，但在策略深度和预判方面有限。
### 代码
```
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Code {
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
        for (int i = 0, k = 0; i < 13; i ++ ) {
            for (int j = 0; j < 14; j ++, k ++ ) {
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
        for (int i = 0; i < 4; i ++ ) {
            int x = aCells.get(aCells.size() - 1).x + dx[i];
            int y = aCells.get(aCells.size() - 1).y + dy[i];
            if (x >= 0 && x < 13 && y >= 0 && y < 14 && g[x][y] == 0) {
                return i;
            }
        }

        return 0;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println(nextMove(sc.next()));
    }
}

```

### 基于最短路
### 介绍
这个算法引入了Floyd算法来寻找最短路径，并尝试将蛇引导到对手的位置。

它首先标记所有障碍物（包括双方的蛇身），然后计算从蛇头到对手蛇头的最短路径。

这种方法在策略深度上有所提升，可以在一定程度上预测和规划蛇的移动，进攻性明显比较激进，在敌方附近障碍物较多时有比较好的效果。
### 代码
```
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Code {
    public static int INT = 0x3f3f3f3f;
    public static int[][] path;
    public static int[][] g = new int[13][14];
    public static int pathLen = -1;
    public static boolean flag = true;
    public static int nextDirection = -1;

    static class Cell {
        public int x, y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static boolean check_tail_increasing(int step) {
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    public static List<Cell> getCells(int sx, int sy, String steps) {
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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println(nextMove(sc.next()));
    }

    public static Integer nextMove(String input) {
        String[] strs = input.split("#");
        for (int i = 0, k = 0; i < 13; i++) {
            for (int j = 0; j < 14; j++, k++) {
                g[i][j] = strs[0].charAt(k) == '1' ? 1 : 0;
            }
        }

        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);
        List<Cell> aCells = getCells(aSx, aSy, strs[3]);
        List<Cell> bCells = getCells(Integer.parseInt(strs[4]), Integer.parseInt(strs[5]), strs[6]);

        for (Cell c : aCells) g[c.x][c.y] = 2;
        for (Cell c : bCells) g[c.x][c.y] = 3;

        int aHeadX = aCells.get(aCells.size() - 1).x;
        int aHeadY = aCells.get(aCells.size() - 1).y;

        int[][] matrix = new int[13 * 14][13 * 14];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = INT;
            }
        }

        path = new int[matrix.length][matrix.length];
        initializeMatrix(matrix, aHeadX, aHeadY);
        floyd(matrix, aHeadX * 14 + aHeadY);

        return nextDirection != -1 ? nextDirection : 0;
    }

    private static void initializeMatrix(int[][] matrix, int aHeadX, int aHeadY) {
        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 14; j++) {
                if (g[i][j] != 0) continue;
                for (int d = 0; d < 4; d++) {
                    int mx = i + dx[d], my = j + dy[d];
                    if (mx >= 0 && mx < 13 && my >= 0 && my < 14 && g[mx][my] == 0) {
                        matrix[i * 14 + j][mx * 14 + my] = 1;
                        matrix[mx * 14 + my][i * 14 + j] = 1;
                    }
                }
            }
        }
    }

    public static void floyd(int[][] matrix, Integer sources) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                path[i][j] = -1;
            }
        }

        for (int m = 0; m < matrix.length; m++) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix.length; j++) {
                    if (matrix[i][m] + matrix[m][j] < matrix[i][j]) {
                        matrix[i][j] = matrix[i][m] + matrix[m][j];
                        path[i][j] = m;
                    }
                }
            }
        }

        int minLength = INT, position = -1;
        for (int i = 0; i < matrix.length; i++) {
            if (g[i / 14][i % 14] == 3 && matrix[sources][i] < minLength) {
                minLength = matrix[sources][i];
                position = i;
            }
        }
        determineNextDirection(sources, position);
    }

    private static void determineNextDirection(int sources, int position) {
        if (position != -1) {
            int headX = sources / 14, headY = sources % 14;
            int nextX = position / 14, nextY = position % 14;
            int dx = nextX - headX, dy = nextY - headY;
            if (dx == -1) nextDirection = 0;
            else if (dy == 1) nextDirection = 1;
            else if (dx == 1) nextDirection = 2;
            else if (dy == -1) nextDirection = 3;
        }
    }
}

```

### 基于贪心+搜索
### 介绍
这个算法在选择移动方向时，会贪心地考虑四个方向中哪一个方向有最大的空间。它通过深度优先搜索（DFS）来估算在每个方向上可用的空间大小。这样的策略既考虑了避开障碍物，也尽量选择空旷的区域移动，以增加生存的可能性。
### 代码
```
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Code {
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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println(nextMove(sc.next()));
    }
}
```

### 基于Minimax Search+alpha-beta剪枝优化
### 介绍
Minimax Search通过模拟玩家和对手的所有可能的移动来预测最佳移动，以达到最优的游戏结果。Minimax Search算法和alpha-beta剪枝结合使用，允许算法在保持策略最优化的同时，减少计算量，确保在实时或近实时的环境中快速做出最佳决策。
### 代码
```
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Code {
    static class Cell {
        public int x, y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static List<Cell> aCells = new LinkedList<>();
    private static List<Cell> bCells = new LinkedList<>();

    private static final int DEPTH = 10;

    private static final int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};

    private static int step; // 回合数

    private static int move = -1;

    // 检验当前回合，长度是否增加  true 增加, 增加时-头部移动,尾部不变, 不增加-头部移动,尾部删除
    private static boolean checkTailIncreasing(int step) {
        if (step <= 10) return true;    // 前10回合每回合长度+1
        return step % 3 == 1;    // 10回合之后没三回合长度+1
    }

    // 通过操作字符串 返回玩家位置list      起始坐标         玩家操作信息字符串
    public static List<Cell> getCells(int sx, int sy, String steps) {
        List<Cell> res = new LinkedList<>();
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x, y));
        for (int i = 0; i < steps.length(); i++) {
            int d = steps.charAt(i) - '0';
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x, y));
            if (!checkTailIncreasing(++step)) { // 长度不增加,
                res.remove(0);
            }
        }
        return res;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println(nextMove(sc.next()));
    }

    // 地图#自己起始横坐标#自己起始纵坐标#(自己操作)#对手起始横坐标#对手起始纵坐标#(对手操作)
    public static Integer nextMove(String input) {
        String[] strs = input.split("#");    // (#拼接)   棋盘(0/1)#a玩家起始x坐标#a玩家起始y坐标   // 对于棋盘来说,只有可走不可走(0/1)
        int[][] g = new int[13][14];    // 棋盘中 0:可走位置 1:不可走位置
        // 棋盘 13 * 14
        for (int i = 0, k = 0; i < 13; i++) {
            for (int j = 0; j < 14; j++, k++) {
                if (strs[0].charAt(k) == '1') {    // 棋盘中的墙
                    g[i][j] = 1;
                }
            }
        }

        // 起始坐标
        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);
        int bSx = Integer.parseInt(strs[4]), bSy = Integer.parseInt(strs[5]);

        // 把操作 转换为🐍
        aCells = getCells(aSx, aSy, strs[3].substring(1, strs[3].length() - 1)); // (1010101)
        bCells = getCells(bSx, bSy, strs[6].substring(1, strs[6].length() - 1));

        // 回合数 玩家移动次数
        step = strs[3].length() - 2;

        // 将初始🐍转换为地图信息
        for (Cell c : aCells) g[c.x][c.y] = 1;    // a玩家游戏位置
        for (Cell c : bCells) g[c.x][c.y] = 1;    // b玩家游戏位置

        // 特殊情况处理 -----------------------------------
        // 玩家可走当前可走方向数量只有4种 0, 1, 2, 3
        int moveNumber = moveNumber(g, aCells);
        if (moveNumber == 0) { // 0种 表示已经输, 特殊处理, 无需minmax, 随便返回一个方向即可
            return 0;
        }
        if (moveNumber == 1)  // 1种 只能这样走, 特殊处理, 无需minmax, 返回此时能走的方向
            for (int i = 0; i < 4; i++) {
                int x = aCells.get(aCells.size() - 1).x + dx[i];
                int y = aCells.get(aCells.size() - 1).y + dy[i];
                if (isMove(g, x, y))
                    return i;
            }

        int depth = DEPTH; // 深度
        max(g, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);

        return move; // 返回操作
    }

    // 棋盘中 0:可走位置 1:玩家位置
    // minimax算法实现          棋盘   深度: depth回合        α剪枝 β剪枝
    public static int max(int[][] g, int depth, int alpha, int beta) {
        step++; // 回合数 ++;

        int score = checkScore(g, aCells, bCells, depth); // 计算分数
        if (score <= 11) return score; // 必输的局
        if (depth == 0) return score; // 走到最底层, 返回全局分数

        // move
        int i = 0;
        for (i = 0; i < 4; i++) {
            int x = aCells.get(aCells.size() - 1).x + dx[i];
            int y = aCells.get(aCells.size() - 1).y + dy[i];
            if (!isMove(g, x, y)) continue;
            Cell cell = null;
            g[x][y] = 1;
            aCells.add(new Cell(x, y)); // 更新玩家位置信息, 玩家位置信息为全局变量

            if (!checkTailIncreasing(step)) { // 长度不增加
                cell = new Cell(aCells.get(0).x, aCells.get(0).y);
                g[cell.x][cell.y] = 0;
                aCells.remove(0);
            }

            int value = min(g, depth, alpha, beta, score);

            // 还原现场
            g[x][y] = 0;
            aCells.remove(aCells.size() - 1);
            if (cell != null) {
                aCells.add(0, cell);
                g[cell.x][cell.y] = 1;
            }

            // α剪枝 , 再分数判断中进行方向判断
            // alpha = Math.max(alpha, value);
            if (value > alpha) {
                alpha = value;
                if (depth == DEPTH)
                    move = i;
            }
            if (alpha >= beta) {
                return beta;
            }
        }
        return alpha;
    }

    public static int min(int[][] g, int depth, int alpha, int beta, int aScore) {

        // b落子
        for (int i = 0; i < 4; i++) {
            int x = bCells.get(bCells.size() - 1).x + dx[i];
            int y = bCells.get(bCells.size() - 1).y + dy[i];

            // 判断位置是否合法(是否能走), 属于分数的范畴,直接失败的操作,单独提取出来
            if (!isMove(g, x, y)) continue;

            // 操作
            Cell cell = null;
            g[x][y] = 1;
            bCells.add(new Cell(x, y));

            if (!checkTailIncreasing(step)) { // 长度不增加
                cell = new Cell(bCells.get(0).x, bCells.get(0).y);
                g[cell.x][cell.y] = 0;
                bCells.remove(0);

            }

            int value = max(g, depth - 1, alpha, beta);
            // 还原现场
            step--; // 回去,回合数也 --;
            g[x][y] = 0;
            bCells.remove(bCells.size() - 1);
            if (cell != null) {
                bCells.add(0, cell);
                g[cell.x][cell.y] = 1;
            }

            // β剪枝
            beta = Math.min(beta, value);
            if (alpha >= beta) {
                return alpha;
            }
        }
        return beta;
    }

    // 下个位置是可移动
    public static boolean isMove(int[][] g, int x, int y) {
        // 越界
        if (x < 0 || x >= 13 || y < 0 || y >= 14) return false;
        // 碰撞 0:可走位置 1:不可走 玩家位置,障碍物
        if (g[x][y] == 1) return false;

        return true;
    }

    // 此位置下一步可走方向数量
    public static int moveNumber(int[][] g, List<Cell> playerCells) {
        int res = 0;
        for (int i = 0; i < 4; i++) {
            int x = playerCells.get(playerCells.size() - 1).x + dx[i];
            int y = playerCells.get(playerCells.size() - 1).y + dy[i];
            if (isMove(g, x, y))
                res++;
        }
        return res;
    }

    // 只考虑自己
    // 计算分数 评估函数( 层数 * 可移动方向数量)             自己的信息       对手的信息
    public static int checkScore(int[][] g, List<Cell> playerCells, List<Cell> foe, int depth) {
        // 失败  玩家四个方法无法移动, 失败的情况归属到一般情况中  <= 11
        if (moveNumber(g, playerCells) == 0) return (DEPTH - depth + 1) * 1;

        // 返回当前位置可走步数 (小分数)    扩大可走位置的倍数
        return (DEPTH - depth + 1) * (int) (Math.pow(moveNumber(g, playerCells) + 1, 2)) + 11;

    }
}
```
