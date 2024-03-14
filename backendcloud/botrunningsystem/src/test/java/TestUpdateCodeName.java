import java.util.Random;

public class TestUpdateCodeName {
    private final static String code = "import java.io.File;\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.List;\n" +
            "import java.util.Scanner;\n" +
            "\n" +
            "public class Code {\n" +
            "    static class Cell {\n" +
            "        public int x, y;\n" +
            "\n" +
            "        public Cell(int x, int y) {\n" +
            "            this.x = x;\n" +
            "            this.y = y;\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    private static List<Cell> aCells = new LinkedList<>();\n" +
            "    private static List<Cell> bCells = new LinkedList<>();\n" +
            "\n" +
            "    private static final int DEPTH = 10;\n" +
            "\n" +
            "    private static final int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};\n" +
            "\n" +
            "    private static int step; // 回合数\n" +
            "\n" +
            "    private static int move = -1;\n" +
            "\n" +
            "    // 检验当前回合，长度是否增加  true 增加, 增加时-头部移动,尾部不变, 不增加-头部移动,尾部删除\n" +
            "    private static boolean checkTailIncreasing(int step) {\n" +
            "        if (step <= 10) return true;    // 前10回合每回合长度+1\n" +
            "        return step % 3 == 1;    // 10回合之后没三回合长度+1\n" +
            "    }\n" +
            "\n" +
            "    // 通过操作字符串 返回玩家位置list      起始坐标         玩家操作信息字符串\n" +
            "    public static List<Cell> getCells(int sx, int sy, String steps) {\n" +
            "        List<Cell> res = new LinkedList<>();\n" +
            "        int x = sx, y = sy;\n" +
            "        int step = 0;\n" +
            "        res.add(new Cell(x, y));\n" +
            "        for (int i = 0; i < steps.length(); i++) {\n" +
            "            int d = steps.charAt(i) - '0';\n" +
            "            x += dx[d];\n" +
            "            y += dy[d];\n" +
            "            res.add(new Cell(x, y));\n" +
            "            if (!checkTailIncreasing(++step)) { // 长度不增加,\n" +
            "                res.remove(0);\n" +
            "            }\n" +
            "        }\n" +
            "        return res;\n" +
            "    }\n" +
            "\n" +
            "    public static void main(String[] args) {\n" +
            "        Scanner sc = new Scanner(System.in);\n" +
            "        System.out.println(nextMove(sc.next()));\n" +
            "    }\n" +
            "\n" +
            "    // 地图#自己起始横坐标#自己起始纵坐标#(自己操作)#对手起始横坐标#对手起始纵坐标#(对手操作)\n" +
            "    public static Integer nextMove(String input) {\n" +
            "        String[] strs = input.split(\"#\");    // (#拼接)   棋盘(0/1)#a玩家起始x坐标#a玩家起始y坐标   // 对于棋盘来说,只有可走不可走(0/1)\n" +
            "        int[][] g = new int[13][14];    // 棋盘中 0:可走位置 1:不可走位置\n" +
            "        // 棋盘 13 * 14\n" +
            "        for (int i = 0, k = 0; i < 13; i++) {\n" +
            "            for (int j = 0; j < 14; j++, k++) {\n" +
            "                if (strs[0].charAt(k) == '1') {    // 棋盘中的墙\n" +
            "                    g[i][j] = 1;\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        // 起始坐标\n" +
            "        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);\n" +
            "        int bSx = Integer.parseInt(strs[4]), bSy = Integer.parseInt(strs[5]);\n" +
            "\n" +
            "        // 把操作 转换为\uD83D\uDC0D\n" +
            "        aCells = getCells(aSx, aSy, strs[3].substring(1, strs[3].length() - 1)); // (1010101)\n" +
            "        bCells = getCells(bSx, bSy, strs[6].substring(1, strs[6].length() - 1));\n" +
            "\n" +
            "        // 回合数 玩家移动次数\n" +
            "        step = strs[3].length() - 2;\n" +
            "\n" +
            "        // 将初始\uD83D\uDC0D转换为地图信息\n" +
            "        for (Cell c : aCells) g[c.x][c.y] = 1;    // a玩家游戏位置\n" +
            "        for (Cell c : bCells) g[c.x][c.y] = 1;    // b玩家游戏位置\n" +
            "\n" +
            "        // 特殊情况处理 -----------------------------------\n" +
            "        // 玩家可走当前可走方向数量只有4种 0, 1, 2, 3\n" +
            "        int moveNumber = moveNumber(g, aCells);\n" +
            "        if (moveNumber == 0) { // 0种 表示已经输, 特殊处理, 无需minmax, 随便返回一个方向即可\n" +
            "            return 0;\n" +
            "        }\n" +
            "        if (moveNumber == 1)  // 1种 只能这样走, 特殊处理, 无需minmax, 返回此时能走的方向\n" +
            "            for (int i = 0; i < 4; i++) {\n" +
            "                int x = aCells.get(aCells.size() - 1).x + dx[i];\n" +
            "                int y = aCells.get(aCells.size() - 1).y + dy[i];\n" +
            "                if (isMove(g, x, y))\n" +
            "                    return i;\n" +
            "            }\n" +
            "\n" +
            "        int depth = DEPTH; // 深度\n" +
            "        max(g, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);\n" +
            "\n" +
            "        return move; // 返回操作\n" +
            "    }\n" +
            "\n" +
            "    // 棋盘中 0:可走位置 1:玩家位置\n" +
            "    // minimax算法实现          棋盘   深度: depth回合        α剪枝 β剪枝\n" +
            "    public static int max(int[][] g, int depth, int alpha, int beta) {\n" +
            "        step++; // 回合数 ++;\n" +
            "\n" +
            "        int score = checkScore(g, aCells, bCells, depth); // 计算分数\n" +
            "        if (score <= 11) return score; // 必输的局\n" +
            "        if (depth == 0) return score; // 走到最底层, 返回全局分数\n" +
            "\n" +
            "        // move\n" +
            "        int i = 0;\n" +
            "        for (i = 0; i < 4; i++) {\n" +
            "            int x = aCells.get(aCells.size() - 1).x + dx[i];\n" +
            "            int y = aCells.get(aCells.size() - 1).y + dy[i];\n" +
            "            if (!isMove(g, x, y)) continue;\n" +
            "            Cell cell = null;\n" +
            "            g[x][y] = 1;\n" +
            "            aCells.add(new Cell(x, y)); // 更新玩家位置信息, 玩家位置信息为全局变量\n" +
            "\n" +
            "            if (!checkTailIncreasing(step)) { // 长度不增加\n" +
            "                cell = new Cell(aCells.get(0).x, aCells.get(0).y);\n" +
            "                g[cell.x][cell.y] = 0;\n" +
            "                aCells.remove(0);\n" +
            "            }\n" +
            "\n" +
            "            int value = min(g, depth, alpha, beta, score);\n" +
            "\n" +
            "            // 还原现场\n" +
            "            g[x][y] = 0;\n" +
            "            aCells.remove(aCells.size() - 1);\n" +
            "            if (cell != null) {\n" +
            "                aCells.add(0, cell);\n" +
            "                g[cell.x][cell.y] = 1;\n" +
            "            }\n" +
            "\n" +
            "            // α剪枝 , 再分数判断中进行方向判断\n" +
            "            // alpha = Math.max(alpha, value);\n" +
            "            if (value > alpha) {\n" +
            "                alpha = value;\n" +
            "                if (depth == DEPTH)\n" +
            "                    move = i;\n" +
            "            }\n" +
            "            if (alpha >= beta) {\n" +
            "                return beta;\n" +
            "            }\n" +
            "        }\n" +
            "        return alpha;\n" +
            "    }\n" +
            "\n" +
            "    public static int min(int[][] g, int depth, int alpha, int beta, int aScore) {\n" +
            "\n" +
            "        // b落子\n" +
            "        for (int i = 0; i < 4; i++) {\n" +
            "            int x = bCells.get(bCells.size() - 1).x + dx[i];\n" +
            "            int y = bCells.get(bCells.size() - 1).y + dy[i];\n" +
            "\n" +
            "            // 判断位置是否合法(是否能走), 属于分数的范畴,直接失败的操作,单独提取出来\n" +
            "            if (!isMove(g, x, y)) continue;\n" +
            "\n" +
            "            // 操作\n" +
            "            Cell cell = null;\n" +
            "            g[x][y] = 1;\n" +
            "            bCells.add(new Cell(x, y));\n" +
            "\n" +
            "            if (!checkTailIncreasing(step)) { // 长度不增加\n" +
            "                cell = new Cell(bCells.get(0).x, bCells.get(0).y);\n" +
            "                g[cell.x][cell.y] = 0;\n" +
            "                bCells.remove(0);\n" +
            "\n" +
            "            }\n" +
            "\n" +
            "            int value = max(g, depth - 1, alpha, beta);\n" +
            "            // 还原现场\n" +
            "            step--; // 回去,回合数也 --;\n" +
            "            g[x][y] = 0;\n" +
            "            bCells.remove(bCells.size() - 1);\n" +
            "            if (cell != null) {\n" +
            "                bCells.add(0, cell);\n" +
            "                g[cell.x][cell.y] = 1;\n" +
            "            }\n" +
            "\n" +
            "            // β剪枝\n" +
            "            beta = Math.min(beta, value);\n" +
            "            if (alpha >= beta) {\n" +
            "                return alpha;\n" +
            "            }\n" +
            "        }\n" +
            "        return beta;\n" +
            "    }\n" +
            "\n" +
            "    // 下个位置是可移动\n" +
            "    public static boolean isMove(int[][] g, int x, int y) {\n" +
            "        // 越界\n" +
            "        if (x < 0 || x >= 13 || y < 0 || y >= 14) return false;\n" +
            "        // 碰撞 0:可走位置 1:不可走 玩家位置,障碍物\n" +
            "        if (g[x][y] == 1) return false;\n" +
            "\n" +
            "        return true;\n" +
            "    }\n" +
            "\n" +
            "    // 此位置下一步可走方向数量\n" +
            "    public static int moveNumber(int[][] g, List<Cell> playerCells) {\n" +
            "        int res = 0;\n" +
            "        for (int i = 0; i < 4; i++) {\n" +
            "            int x = playerCells.get(playerCells.size() - 1).x + dx[i];\n" +
            "            int y = playerCells.get(playerCells.size() - 1).y + dy[i];\n" +
            "            if (isMove(g, x, y))\n" +
            "                res++;\n" +
            "        }\n" +
            "        return res;\n" +
            "    }\n" +
            "\n" +
            "    // 只考虑自己\n" +
            "    // 计算分数 评估函数( 层数 * 可移动方向数量)             自己的信息       对手的信息\n" +
            "    public static int checkScore(int[][] g, List<Cell> playerCells, List<Cell> foe, int depth) {\n" +
            "        // 失败  玩家四个方法无法移动, 失败的情况归属到一般情况中  <= 11\n" +
            "        if (moveNumber(g, playerCells) == 0) return (DEPTH - depth + 1) * 1;\n" +
            "\n" +
            "        // 返回当前位置可走步数 (小分数)    扩大可走位置的倍数\n" +
            "        return (DEPTH - depth + 1) * (int) (Math.pow(moveNumber(g, playerCells) + 1, 2)) + 11;\n" +
            "\n" +
            "    }\n" +
            "}";

    public static void main(String[] args) {
        for (int i = 0; i < 2; i ++) {
            test();
        }
    }

    private static void test() {
        Random random = new Random();
        System.out.println(updateCode(code, random.nextInt(1000)));
    }

    private static String updateCode(String code, int randomNumber) {
//        int i = code.indexOf("class Code");
//        int j = i + "class Code".length();
//        int k = i + "class Code ".length() - 1;
//        return code.substring(0, j) + randomNumber + code.substring(k);
        int k = code.indexOf(" {");
        return code.substring(0, k) + randomNumber + code.substring(k);
    }
}
