import { GameObject } from "./GameObject";
import { Wall } from "./Wall";

export class GameMap extends GameObject {
    constructor(ctx, parent) { // ctx:画布 parent:画布的父元素
        super();

        this.ctx = ctx;
        this.parent = parent;
        this.L = 0; // 正方形边长

        this.row = 13;
        this.col = 13;

        this.inner_walls_count = 20;
        this.walls = [];
    }

    check_connective(g, x1, y1, x2, y2) {
        if (x1 == x2 && y1 == y2) {
            return true;
        }

        g[x1][y1] = true;

        let dx = [-1, 0, 1, 0], dy = [0, 1, 0, -1];

        for (let i = 0; i < 4; i++) {
            let x = x1 + dx[i], y = y1 + dy[i];
            if (!g[x][y] && this.check_connective(g, x, y, x2, y2)) {
                return true;
            }
        }

        return false;
    }

    create_walls() {
        const g = []; // 标记障碍物
        for (let r = 0; r < this.row; r++) {
            g[r] = [];
            for (let c = 0; c < this.col; c++) {
                g[r][c] = false;
            }
        }

        // 给四周添加障碍物
        for (let r = 0; r < this.row; r++) {
            g[r][0] = g[r][this.col - 1] = true;
        }

        for (let c = 0; c < this.col; c++) {
            g[0][c] = g[this.row - 1][c] = true;
        }

        // 创建内部随机障碍物
        // 保证双方公平，对称放置。
        for (let i = 0; i < this.inner_walls_count / 2; i++) {
            for (let j = 0; j < 1000; j++) { // 避免重复，随机1000次
                let r = parseInt(Math.random() * this.row);
                let c = parseInt(Math.random() * this.col);

                if (g[r][c] || g[c][r]) {
                    continue;
                }

                if (r == this.row - 2 && c == 1 || r == 1 && c == this.col - 2) {
                    continue;
                }

                g[r][c] = g[c][r] = true;
                break;
            }
        }

        const copy_g = JSON.parse(JSON.stringify(g));
        if (!this.check_connective(copy_g, this.row - 2, 1, 1, this.col - 2)) {
            return false;
        }

        for (let r = 0; r < this.row; r++) {
            for (let c = 0; c < this.col; c++) {
                if (g[r][c]) {
                    this.walls.push(new Wall(r, c, this));
                }
            }
        }

        return true;
    }

    start() {
        for (let i = 0; i < 1000; i++) {
            if (this.create_walls()) {
                break;
            }
        }
    }

    update_size() {
        // 由于Math是浮点运算，canvas是整数渲染，未避免出现微小空白，所以解析成整数
        this.L = parseInt(Math.min(this.parent.clientWidth / this.col, this.parent.clientHeight / this.row));
        this.ctx.canvas.width = this.L * this.col;
        this.ctx.canvas.height = this.L * this.row;
    }

    update() {
        this.update_size();
        this.render();
    }

    // 渲染函数
    render() {
        const color_even = "#ADE457", color_odd = "#A5DE4E";
        for (let r = 0; r < this.row; r++) {
            for (let c = 0; c < this.col; c++) {
                if ((r + c) % 2 == 0) {
                    this.ctx.fillStyle = color_even;
                } else {
                    this.ctx.fillStyle = color_odd;
                }
                this.ctx.fillRect(c * this.L, r * this.L, this.L, this.L);
            }
        }

    }
}