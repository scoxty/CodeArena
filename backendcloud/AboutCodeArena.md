### CodeArena是什么
CodeArena是在线的程序对抗平台，AI 可以根据已有的游戏规则进行比赛一决胜负。
### 有哪些游戏
目前仅提供贪吃蛇游戏，后续会新增新的游戏。
### 游戏规则 
#### 贪吃蛇游戏规则
简介:

确切地说，这并不是贪吃蛇。 与传统单人贪吃蛇不同的是，本贪吃蛇为双人对战，每回合玩家同时做出决策控制自己的蛇。
玩家在将在13×14的地图中操纵一条蛇 (蛇是一系列坐标构成的有限不重复有顺序的序列，序列中相邻坐标均相邻，即两坐标的x轴坐标或y轴坐标相同，序列中第一个坐标代表蛇头) ，通过控制蛇头的朝向 (东、南、西和北) 来控制蛇。蛇以恒定的速度前进 (前进即为序列头插入蛇头指向方向下一格坐标，并删除序列末尾坐标)。蛇的初始位置在网格中的左下角 (地图位置[13,1]) 与右上角 (地图位置[1,14])，初始长度为1格。与传统贪吃蛇不同，本游戏在网格中并没有豆子，但蛇会自动长大 (长大即为不删除序列末尾坐标的前进) ，前10回合每回合长度增加1，从第11回合开始，每3回合长度增加1。

如何判断输赢：

1.蛇头撞到障碍物、撞到自己或者他人即会死亡

2.任何一条蛇死亡时，游戏结束。

3.若蛇同时死亡，判定为平局，否则先死的一方输，另一方赢。
### 游戏交互方式 
#### 概述 
本游戏目前作为CodeArena的第一款游戏，目前只支持用户使用Java编写Bot逻辑参加比赛，或者选择亲自出战。
#### 具体交互内容 
##### bot参赛 
每回合bot接收一个int变量，表示己方蛇的移动方向。
具体格式如下：（0，1，2，3分别表示 上，右，下，左），样例程序中提供了获取游戏对局信息的各种接口，你只需要编写nextMove方法。
```
public Integer nextMove(String input) {
    return 0;//则Bot贪吃蛇将一直向上走
}
```

样例：

Java
```Java
package com.codearena.botrunningsystem.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AIBot implements java.util.function.Supplier<Integer> {
    static class Cell {
        public int x, y;
        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // 检验当前回合，长度是否增加  true 增加, 增加时-头部移动,尾部不变, 不增加-头部移动,尾部删除
    private boolean check_tail_increasing(int step) {
        if (step <= 10) return true; // 前10回合每回合长度+1
        return step % 3 == 1; //10回合之后每三回合长度+1
    }

    // 根据收集的玩家操作, 计算并返回玩家的位置
    public List<Cell> getCells(int sx, int sy, String steps) {
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
    public Integer nextMove(String input) {
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

    @Override
    public Integer get() {
        File file = new File("AI_Input.txt");
        try {
            Scanner sc = new Scanner(file);
            return nextMove(sc.next());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
```

Python

```Python
class Cell:
    def __init__(self, x, y):
        self.x = x
        self.y = y

def check_tail_increasing(step):
    if step <= 10:
        return True
    return step % 3 == 1

def get_cells(sx, sy, steps):
    steps = steps[1:-1]  # 去除括号
    res = []

    dx = [-1, 0, 1, 0]
    dy = [0, 1, 0, -1]
    x, y = sx, sy
    step = 0

    res.append(Cell(x, y))
    for d in steps:
        direction = int(d)
        x += dx[direction]
        y += dy[direction]
        res.append(Cell(x, y))
        if not check_tail_increasing(step + 1):
            res.pop(0)
        step += 1

    return res

def next_move(input_str):
    strs = input_str.split('#')
    g = [[0 for _ in range(14)] for _ in range(13)]

    for i, k in enumerate(strs[0]):
        if k == '1':
            g[i // 14][i % 14] = 1

    a_sx, a_sy = int(strs[1]), int(strs[2])
    b_sx, b_sy = int(strs[4]), int(strs[5])

    a_cells = get_cells(a_sx, a_sy, strs[3])
    b_cells = get_cells(b_sx, b_sy, strs[6])

    for cell in a_cells + b_cells:
        g[cell.x][cell.y] = 1

    dx = [-1, 0, 1, 0]
    dy = [0, 1, 0, -1]
    for i in range(4):
        x = a_cells[-1].x + dx[i]
        y = a_cells[-1].y + dy[i]
        if 0 <= x < 13 and 0 <= y < 14 and g[x][y] == 0:
            return i

    return 0

def main():
    try:
        with open("/botrunning/input.txt", "r") as file:
            input_str = file.read().strip()
            print(next_move(input_str))
    except FileNotFoundError as e:
        print(e)

if __name__ == "__main__":
    main()
```

C

```C
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct Cell {
    int x, y;
} Cell;

// 检验当前回合，长度是否增加
int check_tail_increasing(int step) {
    if (step <= 10) return 1; // 前10回合每回合长度+1
    return step % 3 == 1;     // 10回合之后每三回合长度+1
}

// 根据收集的玩家操作，计算并返回玩家的位置
Cell* getCells(int sx, int sy, char* steps, int* cellCount) {
    int dx[] = {-1, 0, 1, 0};
    int dy[] = {0, 1, 0, -1};
    int x = sx, y = sy;
    int step = 0;
    Cell* res = (Cell*)malloc(sizeof(Cell) * strlen(steps));
    int count = 0;
    res[count++] = (Cell){x, y};

    for (int i = 0; steps[i] != '\0'; i++) {
        int d = steps[i] - '0';
        x += dx[d];
        y += dy[d];
        res[count++] = (Cell){x, y};
        if (!check_tail_increasing(++step)) {
            // 移动数组前移
            for(int j = 0; j < count - 1; j++) {
                res[j] = res[j + 1];
            }
            count--;
        }
    }
    *cellCount = count;
    return res;
}

int main() {
    FILE* file = fopen("/botrunning/input.txt", "r");
    if (file == NULL) {
        perror("File opening failed");
        return EXIT_FAILURE;
    }

    char input[1024];
    fscanf(file, "%s", input);
    fclose(file);

    char* token;
    int g[13][14] = {0};
    int aSx, aSy, bSx, bSy;
    int cellCountA, cellCountB;

    // Parse input
    token = strtok(input, "#");
    for (int i = 0, k = 0; i < 13; i++) {
        for (int j = 0; j < 14; j++, k++) {
            if (token[k] == '1') {
                g[i][j] = 1;
            }
        }
    }

    token = strtok(NULL, "#");
    aSx = atoi(token);
    token = strtok(NULL, "#");
    aSy = atoi(token);
    token = strtok(NULL, "#");
    Cell* aCells = getCells(aSx, aSy, token, &cellCountA);
    
    token = strtok(NULL, "#");
    bSx = atoi(token);
    token = strtok(NULL, "#");
    bSy = atoi(token);
    token = strtok(NULL, "#");
    Cell* bCells = getCells(bSx, bSy, token, &cellCountB);

    // 标记地图
    for (int i = 0; i < cellCountA; i++) {
        g[aCells[i].x][aCells[i].y] = 1;
    }
    for (int i = 0; i < cellCountB; i++) {
        g[bCells[i].x][bCells[i].y] = 1;
    }

    int dx[] = {-1, 0, 1, 0}, dy[] = {0, 1, 0, -1};
    for (int i = 0; i < 4; i++) {
        int x = aCells[cellCountA - 1].x + dx[i];
        int y = aCells[cellCountA - 1].y + dy[i];
        if (x >= 0 && x < 13 && y >= 0 && y < 14 && g[x][y] == 0) {
            printf("%d\n", i);
            break;
        }
    }

    free(aCells);
    free(bCells);

    return 0;
}
```

C++

```Cpp
#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <sstream>

using namespace std;

class Cell {
public:
    int x, y;
    Cell(int x, int y) : x(x), y(y) {}
};

bool check_tail_increasing(int step) {
    if (step <= 10) return true;
    return step % 3 == 1;
}

vector<Cell> getCells(int sx, int sy, const string& steps) {
    vector<Cell> res;
    int dx[] = {-1, 0, 1, 0}, dy[] = {0, 1, 0, -1};
    int x = sx, y = sy;
    int step = 0;
    res.emplace_back(x, y);
    for (char d : steps) {
        int dir = d - '0';
        x += dx[dir];
        y += dy[dir];
        res.emplace_back(x, y);
        if (!check_tail_increasing(++step)) {
            res.erase(res.begin());
        }
    }
    return res;
}

int nextMove(const string& input) {
    stringstream ss(input);
    string token;
    vector<string> tokens;
    while (getline(ss, token, '#')) {
        tokens.push_back(token);
    }

    int g[13][14] = {0};
    for (int i = 0, k = 0; i < 13; ++i) {
        for (int j = 0; j < 14; ++j, ++k) {
            if (tokens[0][k] == '1') {
                g[i][j] = 1;
            }
        }
    }

    int aSx = stoi(tokens[1]), aSy = stoi(tokens[2]);
    int bSx = stoi(tokens[4]), bSy = stoi(tokens[5]);

    auto aCells = getCells(aSx, aSy, tokens[3].substr(1, tokens[3].size() - 2));
    auto bCells = getCells(bSx, bSy, tokens[6].substr(1, tokens[6].size() - 2));

    for (const auto& c : aCells) g[c.x][c.y] = 1;
    for (const auto& c : bCells) g[c.x][c.y] = 1;

    int dx[] = {-1, 0, 1, 0}, dy[] = {0, 1, 0, -1};
    for (int i = 0; i < 4; ++i) {
        int x = aCells.back().x + dx[i];
        int y = aCells.back().y + dy[i];
        if (x >= 0 && x < 13 && y >= 0 && y < 14 && g[x][y] == 0) {
            return i;
        }
    }

    return 0;
}

int main() {
    ifstream file("/botrunning/input.txt");
    if (!file.is_open()) {
        cerr << "File not found" << endl;
        return -1;
    }

    string input;
    file >> input;
    cout << nextMove(input) << endl;

    return 0;
}

```
##### 人类参赛 
键盘输入w、a、s、d控制己方蛇的移动方向。