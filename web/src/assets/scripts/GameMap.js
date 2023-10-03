import { GameObject } from "./GameObject";
import { Snake } from "./Snake";
import { Wall } from "./Wall";

export class GameMap extends GameObject {
    constructor(ctx, parent) { // ctx:画布 parent:画布的父元素
        super();

        this.ctx = ctx;
        this.parent = parent;

        this.L = 0; // 地图由小正方形组成，L表示小正方形边长
        // 让地图长宽的奇偶性不同，避免出现两两同时到达一个位置的情况
        /**
         * A:(11, 1) 偶、奇、偶、奇...
         * B:(1, 12) 奇、偶、奇、偶...
         */
        this.row = 13; // 行数
        this.col = 14; // 列数

        this.inner_walls_count = 20;
        this.walls = [];

        this.snakes = [
            new Snake({ id: 0, color: "#4876EC", r: this.row - 2, c: 1 }, this),
            new Snake({ id: 1, color: "#F94848", r: 1, c: this.col - 2 }, this),
        ];
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
        // 保证双方公平，按中心对称的方式放置。
        for (let i = 0; i < this.inner_walls_count / 2; i++) {
            for (let j = 0; j < 1000; j++) { // 避免重复，随机1000次
                let r = parseInt(Math.random() * this.row);
                let c = parseInt(Math.random() * this.col);

                if (g[r][c] || g[this.row - 1 - r][this.col - 1 - c]) {
                    continue;
                }

                // 左下角和右上角为玩家起点，不能有障碍物
                if (r == this.row - 2 && c == 1 || r == 1 && c == this.col - 2) {
                    continue;
                }

                g[r][c] = g[this.row - 1 - r][this.col - 1 - c] = true;
                break;
            }
        }

        const copy_g = JSON.parse(JSON.stringify(g)); // 深拷贝
        // 判断是否联通
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

    add_listening_events() {
        this.ctx.canvas.focus();

        const [snake0, snake1] = this.snakes;
        this.ctx.canvas.addEventListener("keydown", e => {
            if (e.key === 'w')
                snake0.set_direction(0);
            else if (e.key === 'd')
                snake0.set_direction(1);
            else if (e.key === 's')
                snake0.set_direction(2);
            else if (e.key === 'a')
                snake0.set_direction(3);
            else if (e.key === 'ArrowUp')
                snake1.set_direction(0);
            else if (e.key === 'ArrowRight')
                snake1.set_direction(1);
            else if (e.key === 'ArrowDown')
                snake1.set_direction(2);
            else if (e.key === 'ArrowLeft')
                snake1.set_direction(3);
        });
    }

    start() {
        for (let i = 0; i < 1000; i++) {
            if (this.create_walls()) {
                break;
            }
        }
        this.add_listening_events();
    }

    update_size() {
        // 由于Math是浮点运算，canvas是整数渲染，未避免出现微小空白，所以解析成整数
        this.L = parseInt(Math.min(this.parent.clientWidth / this.col, this.parent.clientHeight / this.row));
        this.ctx.canvas.width = this.L * this.col;
        this.ctx.canvas.height = this.L * this.row;
    }

    check_ready() {
        for (const snake of this.snakes) {
            if (snake.status !== "idle" || snake.direction === -1) {
                return false;
            }
        }
        return true;
    }

    next_step() { // 让两条蛇进入下一回合
        for (const snake of this.snakes) {
            snake.next_step();
        }
    }

    check_valid(cell) { // 检测目标位置是否合法
        for (const wall of this.walls) {
            if (wall.r === cell.r && wall.c === cell.c) {
                return false;
            }
        }

        for (const snake of this.snakes) {
            let k = snake.cells.length;
            if (!snake.check_tail_increasing()) { // 当蛇尾会前进时，蛇尾不要判断
                k--;
            }
            for (let i = 0; i < k; i++) {
                if (snake.cells[i].r === cell.r && snake.cells[i].c === cell.c) {
                    return false;
                }
            }
        }

        return true;
    }

    update() {
        this.update_size();
        if (this.check_ready()) {
            this.next_step();
        }
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
                // canvas 行是y，列是x
                this.ctx.fillRect(c * this.L, r * this.L, this.L, this.L);
            }
        }
    }
}