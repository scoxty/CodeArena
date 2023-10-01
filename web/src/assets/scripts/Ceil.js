export class Cell {
    constructor(r, c) {
        this.r = r;
        this.c = c;
        // (r, c)表示小方块在矩阵中的位置
        // (x, y)表示canvas中圆心的位置，canvas中列表示x，行表示y
        this.x = c + 0.5;
        this.y = r + 0.5;
    }
}