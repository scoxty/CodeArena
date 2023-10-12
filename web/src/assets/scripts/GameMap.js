import { GameObject } from "./GameObject";
import { Snake } from "./Snake";
import { Wall } from "./Wall";

export class GameMap extends GameObject {
    constructor(ctx, parent, store) { // ctx:画布 parent:画布的父元素
        super();

        this.ctx = ctx;
        this.parent = parent;
        this.store = store;

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

    create_walls() {
        const g = this.store.state.pk.gamemap;

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
        if (this.store.state.record.is_record) {
            let k = 0;
            const a_steps = this.store.state.record.a_steps;
            const b_steps = this.store.state.record.b_steps;
            const loser = this.store.state.record.record_loser;
            const [snake0, snake1] = this.snakes;
            const interval_id = setInterval(() => {
                if (k >= a_steps.length - 1) {
                    if (loser === "all" || loser === "A") {
                        snake0.status = "die";
                    }
                    if (loser === "all" || loser === "B") {
                        snake1.status = "die";
                    }
                    clearInterval(interval_id);
                } else {
                    snake0.set_direction(parseInt(a_steps[k]));
                    snake1.set_direction(parseInt(b_steps[k]));
                }
                k++;
            }, 300);
        } else {
            this.ctx.canvas.focus();
            this.ctx.canvas.addEventListener("keydown", e => {
                let d = -1;
                if (e.key === 'w')
                    d = 0;
                else if (e.key === 'd')
                    d = 1;
                else if (e.key === 's')
                    d = 2;
                else if (e.key === 'a')
                    d = 3;

                if (d >= 0) {
                    this.store.state.pk.socket.send(JSON.stringify({
                        event: "move",
                        direction: d,
                    }))
                }
            });
        }
    }

    start() {
        this.create_walls()
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